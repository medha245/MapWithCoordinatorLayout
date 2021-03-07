package com.medha.mapwithcoordinatorlayout

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.medha.mapwithcoordinatorlayout.databinding.BorderedEditTextBinding

class BorderedEditTextWithHeader : FrameLayout {
    private var originalTextColor: Int? = null
    private var disabledColor: Int? = null
    private lateinit var binding: BorderedEditTextBinding

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, null)
    }

    constructor(context: Context) : super(context) {
        init(context, null, null)

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {

        init(context, attrs, defStyle)
    }

    fun setError(errorMsg: String) {
        context?.let {
            try {
                val background = MaterialShapes.roundedCornerWithStroke(R.color.red_500,
                    R.color.white, R.dimen.dp_1,
                    R.dimen.margin_smallest, it)
                binding.etBorderedLayout?.background = background
            }catch(ex:Exception){

            }
        }
        binding.tvBorderedError.visibility = View.VISIBLE
        binding.tvBorderedError.setText(errorMsg)
        binding.tvBorderedError.requestFocus()
    }

    fun removeError() {
        binding.etBorderedLayout?.background =
            ContextCompat.getDrawable(context, R.drawable.et_border_bg)
        binding.tvBorderedError.visibility = View.GONE
        binding.tvBorderedError.setText("")
    }



    fun setDrawableTextAndCta(text: String, drawableLeft: Drawable?, rightDrawable: Drawable?) {
        binding.rightCtaTv.text = text
        binding.rightCtaTv.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, rightDrawable, null)
    }

    fun setCtaTextColor(color: Int) {
        binding.rightCtaTv.setTextColor(ContextCompat.getColor(context, color))
    }

    fun init(context: Context, attrs: AttributeSet?, defStyle: Int?) {
        binding = BorderedEditTextBinding.inflate(LayoutInflater.from(context), this, true)
        originalTextColor = binding.etBordered?.currentTextColor
        disabledColor = ContextCompat.getColor(context, R.color.grey_300)

        binding.etBordered.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.etBorderedLayout?.background = ContextCompat.getDrawable(context, R.drawable.et_border_bg)
                binding.tvBorderedError.visibility = View.GONE
            }
        })

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it,
                R.styleable.BorderedEditTextWithHeader, 0, defStyle ?: 0
            )
            val count = typedArray.indexCount
            for (i in 0..count) {
                try {
                    val attr = typedArray.getIndex(i)

                    when (attr) {
                        R.styleable.BorderedEditTextWithHeader_etHint -> {
                            binding.etBorderedLayout?.hint = typedArray.getString(attr)
                        }
                        R.styleable.BorderedEditTextWithHeader_etText -> {
                            binding.etBordered?.setText(typedArray.getString(attr))
                        }
                        R.styleable.BorderedEditTextWithHeader_android_inputType -> {
                            binding.etBordered?.inputType = typedArray.getInt(attr, 0)
                        }
                        R.styleable.BorderedEditTextWithHeader_android_enabled -> {
                            binding.etBorderedLayout?.isEnabled =
                                typedArray.getBoolean(attr, true)
                        }
                        R.styleable.BorderedEditTextWithHeader_android_longClickable -> {
                            binding.etBordered?.isLongClickable =
                                typedArray.getBoolean(attr, true)
                        }
                        R.styleable.BorderedEditTextWithHeader_android_imeOptions -> {
                            binding.etBordered.imeOptions = typedArray.getInt(attr, 0)
                        }
                        R.styleable.BorderedEditTextWithHeader_android_maxLength -> {
                            val fArray = arrayOfNulls<InputFilter>(1)
                            fArray[0] = InputFilter.LengthFilter(typedArray.getInt(attr, 0))
                            binding.etBordered.setFilters(fArray)

                        }
                        R.styleable.BorderedEditTextWithHeader_drawableLeftCta -> {
                            val drawable = ContextCompat.getDrawable(context, typedArray.getResourceId(attr, 0))
                            binding.rightCtaTv.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable
                                , null)
                        }
                        R.styleable.BorderedEditTextWithHeader_drawableRightCta -> {
                            val drawable = ContextCompat.getDrawable(context, typedArray.getResourceId(attr, 0))
                            binding.rightCtaTv.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
                        }
                        R.styleable.BorderedEditTextWithHeader_ctaText -> {
                            binding.rightCtaTv.text = typedArray.getString(attr)
                        }
                        R.styleable.BorderedEditTextWithHeader_ctaTextColor -> {
                            binding.rightCtaTv.setTextColor(typedArray.getColor(attr, 0))
                        }
                    }
                } catch (ex: IndexOutOfBoundsException) {
                    Log.e("index", ex.toString())
                }

            }
            typedArray.recycle()
        }
    }

    fun setText(msg: String) {
        binding.etBordered.setText(msg)
    }

    fun getText(): Editable? {
        return binding.etBordered.text
    }

    override fun setEnabled(enabled: Boolean) {
        binding.etBordered.isEnabled = enabled

        if (enabled) {
            originalTextColor?.let { binding.etBordered.setTextColor(it) }
        } else {
            disabledColor?.let { binding.etBordered.setTextColor(it) }
        }

    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.etBordered.setOnClickListener(l)
    }

    override fun setOnFocusChangeListener(l: OnFocusChangeListener?) {
        binding.etBordered.setOnFocusChangeListener(l)
    }
}