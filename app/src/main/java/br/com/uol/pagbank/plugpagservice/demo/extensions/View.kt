package br.com.uol.pagbank.plugpagservice.demo.extensions

import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator

private const val ANIMATION_DURATION = 250L

private fun loadAnimationFadeIn(): Animation {
    return AlphaAnimation(0f, 1f).apply {
        interpolator = AccelerateInterpolator()
        setDuration(ANIMATION_DURATION)
    }
}

private fun loadAnimationGetIn(): Animation {
    val fadeIn = loadAnimationFadeIn()
    val moveIn = android.view.animation.TranslateAnimation(0f, 0f, +100f, 0f).apply {
        interpolator = DecelerateInterpolator()
        setDuration(ANIMATION_DURATION)
    }

    return AnimationSet(false).apply {
        addAnimation(fadeIn)
        addAnimation(moveIn)
    }
}

private fun loadAnimationFadeOut(): Animation {
    return AlphaAnimation(1f, 0f).apply {
        interpolator = AccelerateInterpolator()
        setDuration(ANIMATION_DURATION)
    }
}

private fun loadAnimationGetOut(): Animation {
    val fadeOut = loadAnimationFadeOut()
    val moveOut = android.view.animation.TranslateAnimation(0f, 0f, 0f, +100f).apply {
        interpolator = DecelerateInterpolator()
        setDuration(ANIMATION_DURATION)
    }

    return AnimationSet(false).apply {
        addAnimation(fadeOut)
        addAnimation(moveOut)
    }
}

fun View.fadeIn() {
    if (visibility == View.VISIBLE) return

    visibility = View.VISIBLE
    startAnimation(loadAnimationFadeIn())
}

fun View.getIn() {
    if (visibility == View.VISIBLE) return

    visibility = View.VISIBLE
    startAnimation(loadAnimationGetIn())
}

fun View.fadeOut() {
    if (visibility == View.GONE) return

    val fadeOut = loadAnimationFadeOut()
    fadeOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {}
        override fun onAnimationRepeat(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {
            visibility = View.GONE
        }
    })

    startAnimation(fadeOut)
}

fun View.getOut() {
    if (visibility == View.GONE) return

    val getOut = loadAnimationGetOut()
    getOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {}
        override fun onAnimationRepeat(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {
            visibility = View.GONE
        }
    })

    startAnimation(getOut)
}
