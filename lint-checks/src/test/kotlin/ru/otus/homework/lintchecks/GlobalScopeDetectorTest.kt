package ru.otus.homework.lintchecks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

internal class GlobalScopeDetectorTest {

    @Test
    fun `Testing GlobalScope in ViewModel`() {
        lint()
            .allowMissingSdk()
            .files(
                kotlin(
                    """
                        package tests

                        class TestViewModel : ViewModel() {
                            init {
                                GlobalScope.launch{}
                            }
                        }
                    """.trimIndent()
                ),
                kotlin(
                    """
                        package androidx.lifecycle
                        class ViewModel {}
                    """.trimIndent()
                )
            )
            .issues(GlobalScopeDetector.ISSUE)
            .run()
            .expectErrorCount(1)
    }

    @Test
    fun `Testing GlobalScope`() {
        lint()
            .allowMissingSdk()
            .files(
                kotlin(
                    """
                        @file:OptIn(DelicateCoroutinesApi::class)
                        package ru.otus.homework.linthomework.globalscopeusage

                        class GlobalScopeTestCase(private val scope: CoroutineScope) : ViewModel() {

                            fun case1() {
                                GlobalScope.launch {
                                    delay(1000)
                                    println("Hello World")
                                }
                                GlobalScope.actor<String> {
                                    delay(1000)
                                    println("Hello World")
                                }
                            }

                            fun case2() {
                                viewModelScope.launch {
                                    val deferred = GlobalScope.async {
                                        delay(1000)
                                        "Hello World"
                                    }
                                    println(deferred.await())
                                }
                            }

                            fun case3() {
                                scope.launch {
                                    delay(1000)
                                    println("Hello World")
                                }
                            }
                        }
                    """.trimIndent()
                )
            )
            .issues(GlobalScopeDetector.ISSUE)
            .run()
            .expectErrorCount(3)
    }

    @Test
    fun `Testing GlobalScope usage output`() {
        lint()
            .allowMissingSdk()
            .files(
                kotlin(
                    """
                        package tests
                        val scope = GlobalScope
                        class ViewModelTest {
                            init {
                                GlobalScope.launch{ }
                            }
                        }
                    """.trimIndent()
                )
            )
            .issues(GlobalScopeDetector.ISSUE)
            .run()
            .expect(
                """
                    src/tests/ViewModelTest.kt:2: Error: GlobalScope usage isn`t allowed [GlobalScopeUsage]
                    val scope = GlobalScope
                                ~~~~~~~~~~~
                    src/tests/ViewModelTest.kt:5: Error: GlobalScope usage isn`t allowed [GlobalScopeUsage]
                            GlobalScope.launch{ }
                            ~~~~~~~~~~~
                    2 errors, 0 warnings
                """.trimIndent()
            )
    }
}