package com.asha.md360player4android

import com.asha.vrlib.common.MDDirection
import com.asha.vrlib.strategy.projection.AbsProjectionStrategy
import com.asha.vrlib.strategy.projection.IMDProjectionFactory
import com.asha.vrlib.strategy.projection.MultiFishEyeProjection

/**
 * Created by hzqiujiadi on 16/8/20.
 * hzqiujiadi ashqalcn@gmail.com
 */
class CustomProjectionFactory : IMDProjectionFactory {

    override fun createStrategy(mode: Int): AbsProjectionStrategy? {
        when (mode) {
            CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL -> return MultiFishEyeProjection(0.745f, MDDirection.VERTICAL)
            else -> return null
        }
    }

    companion object {

        val CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL = 9611
    }
}
