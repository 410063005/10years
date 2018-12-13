学习 [iosched](https://github.com/google/iosched) 的记录

# 库

## ThreeTenABP

为什么要使用新的时间库？旧的时间库有什么问题？Joda Time 有什么问题？

[GitHub - JakeWharton/ThreeTenABP: An adaptation of the JSR-310 ... https://github.com/JakeWharton/ThreeTenABP](https://github.com/JakeWharton/ThreeTenABP)

[Joda Time's Memory Issue in Android](https://blog.danlew.net/2013/08/20/joda_time_s_memory_issue_in_android/)

[Joda Time's Memory Issue in Android](https://blog.danlew.net/2013/08/20/joda_time_s_memory_issue_in_android/)

```kotlin
class MainApplication : DaggerApplication() {

    override fun onCreate() {
        // ThreeTenBP for times and dates, called before super to be available for objects
        AndroidThreeTen.init(this)
    }
}
```

## Dagger

## ViewModel

ViewModelProviders.of(FragmentActivity activity)

ViewModelProviders.of(FragmentActivity activity, Factory factory)

两者的区别

## DataBinding

DataBindUtil 的用

# Android

Transition 的使用

```kotlin
    private fun setupTransition(binding: ActivityOnboardingBinding) {
        // Transition the logo animation (roughly) from the preview window background.
        // binding.logo 是一个 ImageView
        binding.logo.apply {
            val interpolator =
                AnimationUtils.loadInterpolator(context, interpolator.linear_out_slow_in)
            alpha = 0.4f
            scaleX = 0.8f
            scaleY = 0.8f
            doOnNextLayout {
                translationY = height / 3f
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .interpolator = interpolator
            }
        }
    }
```

# Kotlin
