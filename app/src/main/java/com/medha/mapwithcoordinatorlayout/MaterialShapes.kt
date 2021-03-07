package com.medha.mapwithcoordinatorlayout

import android.content.Context
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import dagger.hilt.android.qualifiers.ApplicationContext

object MaterialShapes {

    fun roundedCornerWithStroke(strokeColor:Int, fillColor:Int, strokeWidth:Int,roundCorner:Int,
                                context: Context
    ): MaterialShapeDrawable {
        val kycCardDrawable = MaterialShapeDrawable()
        kycCardDrawable.setTint(
            ContextCompat.getColor(
                context,
                fillColor
            )
        )
        kycCardDrawable.setStroke(context.resources.getDimension(strokeWidth), ContextCompat.getColor(context, strokeColor))
        kycCardDrawable.paintStyle = Paint.Style.FILL_AND_STROKE
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, context.resources.getDimension(roundCorner))
            .build()
        kycCardDrawable.shapeAppearanceModel = shapeAppearanceModel
        return kycCardDrawable
    }

    fun roundedCornerWithStrokeLeftCorners(strokeColor: Int, fillColor: Int, strokeWidth: Int, roundCorner: Int,
                                           context: Context
    ): MaterialShapeDrawable {
        val kycCardDrawable = MaterialShapeDrawable()
        kycCardDrawable.setTint(
            ContextCompat.getColor(
                context,
                fillColor
            )
        )
        kycCardDrawable.setStroke(context.resources.getDimension(strokeWidth), ContextCompat.getColor(context, strokeColor))
        kycCardDrawable.paintStyle = Paint.Style.FILL_AND_STROKE
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, context.resources.getDimension(roundCorner))
            .setBottomLeftCorner(CornerFamily.ROUNDED, context.resources.getDimension(roundCorner))
            .build()
        kycCardDrawable.shapeAppearanceModel = shapeAppearanceModel
        return kycCardDrawable
    }

    fun roundedCornerWithStrokeRightCorners(strokeColor: Int, fillColor: Int, strokeWidth: Int, roundCorner: Int,
                                            context: Context
    ): MaterialShapeDrawable {
        val kycCardDrawable = MaterialShapeDrawable()
        kycCardDrawable.setTint(
            ContextCompat.getColor(
                context,
                fillColor
            )
        )
        kycCardDrawable.setStroke(context.resources.getDimension(strokeWidth), ContextCompat.getColor(context, strokeColor))
        kycCardDrawable.paintStyle = Paint.Style.FILL_AND_STROKE
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setTopRightCorner(CornerFamily.ROUNDED, context.resources.getDimension(roundCorner))
            .setBottomRightCorner(CornerFamily.ROUNDED, context.resources.getDimension(roundCorner))
            .build()
        kycCardDrawable.shapeAppearanceModel = shapeAppearanceModel
        return kycCardDrawable
    }

    fun roundedCorner( fillColor:Int, roundCorner:Int,
                       @ApplicationContext context: Context
    ): MaterialShapeDrawable {
        val kycCardDrawable = MaterialShapeDrawable()
        kycCardDrawable.setTint(
            ContextCompat.getColor(
                context,
                fillColor
            )
        )
        kycCardDrawable.paintStyle = Paint.Style.FILL
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, context.resources.getDimension(roundCorner))
            .build()
        kycCardDrawable.shapeAppearanceModel = shapeAppearanceModel
        return kycCardDrawable
    }

    fun roundedCorner( fillColor:Int, roundCornerTopLeft:Int,roundCornerTopRight:Int,
                       @ApplicationContext context: Context
    ): MaterialShapeDrawable {
        val shipmentMetricsDrawable = MaterialShapeDrawable()
        shipmentMetricsDrawable.setTint(
            ContextCompat.getColor(
                context,
                fillColor
            ))
        shipmentMetricsDrawable.paintStyle = Paint.Style.FILL
        val shapeAppearanceModel3 = ShapeAppearanceModel()
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, context.resources.getDimension(roundCornerTopLeft))
            .setTopRightCorner(CornerFamily.ROUNDED, context.resources.getDimension(roundCornerTopRight))
            .build()
        shipmentMetricsDrawable.shapeAppearanceModel = shapeAppearanceModel3
        return shipmentMetricsDrawable
    }

    fun roundedCornerBottomWithStroke( fillColor:Int, strokeColor: Int,strokeWidth:Int,roundCornerBottomLeft:Int,roundCornerBottomRight:Int,
                                       @ApplicationContext context: Context
    ): MaterialShapeDrawable {
        val shipmentMetricsDrawable = MaterialShapeDrawable()
        shipmentMetricsDrawable.setTint(
            ContextCompat.getColor(
                context,
                fillColor
            ))
        shipmentMetricsDrawable.paintStyle = Paint.Style.FILL
        shipmentMetricsDrawable.setStroke(context.resources.getDimension(strokeWidth), ContextCompat.getColor(context, strokeColor))
        val shapeAppearanceModel3 = ShapeAppearanceModel()
            .toBuilder()
            .setBottomLeftCorner(CornerFamily.ROUNDED, context.resources.getDimension(roundCornerBottomLeft))
            .setBottomRightCorner(CornerFamily.ROUNDED, context.resources.getDimension(roundCornerBottomRight))
            .build()
        shipmentMetricsDrawable.shapeAppearanceModel = shapeAppearanceModel3
        return shipmentMetricsDrawable
    }
}