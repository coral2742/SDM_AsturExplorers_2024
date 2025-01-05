package sdm.com.asturexplorers


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class RegisterOkTest {

    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun registerOkTest() {
        val materialButton = onView(
            allOf(
                withId(android.R.id.button2), withText("Cerrar"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.ScrollView")),
                        0
                    ),
                    2
                )
            )
        )
        materialButton.perform(scrollTo(), click())

        val bottomNavigationItemView = onView(
            allOf(
                withId(R.id.navigation_mi_perfil), withContentDescription("Mi Perfil"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.navigationbtn),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView.perform(click())

        val materialTextView = onView(
            allOf(
                withId(R.id.txtRegistraAqui), withText("Regístrate aquí"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.fragmentContainerView),
                        0
                    ),
                    18
                ),
                isDisplayed()
            )
        )
        materialTextView.perform(click())

        Thread.sleep(2000)

        val textInputEditText = onView(
            allOf(
                withId(R.id.inputEmailText),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.inputEmail),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        val em="testing" + System.currentTimeMillis() + "@email.com"
        textInputEditText.perform(replaceText(em), closeSoftKeyboard())

        val textInputEditText2 = onView(
            allOf(
                withId(R.id.passwordEditText),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.inputPassword),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText2.perform(replaceText("testing"), closeSoftKeyboard())

        val textInputEditText3 = onView(
            allOf(
                withId(R.id.repePasswordEditText),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.inputRepePassword),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText3.perform(replaceText("testing"), closeSoftKeyboard())

        val materialButton2 = onView(
            allOf(
                withId(R.id.btnSignUp), withText("Crear cuenta"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.fragmentContainerView),
                        0
                    ),
                    6
                ),
                isDisplayed()
            )
        )
        materialButton2.perform(click())

        Thread.sleep(2000)

        val textView = onView(
            allOf(
                withId(R.id.txtEmail), withText(em),
                withParent(withParent(withId(R.id.fragmentContainerView))),
                isDisplayed()
            )
        )
        textView.check(matches(withText(em)))

        val button = onView(
            allOf(
                withId(R.id.btnSignOut), withText("Cerrar sesión"),
                withParent(withParent(withId(R.id.fragmentContainerView))),
                isDisplayed()
            )
        )
        button.check(matches(isDisplayed()))
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
