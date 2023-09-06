package com.sixtyninefourtwenty.materialspinner

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Filterable
import android.widget.FrameLayout
import android.widget.ListAdapter
import androidx.annotation.ArrayRes
import com.sixtyninefourtwenty.materialspinner.databinding.MaterialSpinnerContentBinding
import java.util.function.IntConsumer

@Suppress("unused", "MemberVisibilityCanBePrivate")
class MaterialSpinner @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attributeSet, defStyleAttr) {

    private val binding = MaterialSpinnerContentBinding.inflate(LayoutInflater.from(context), this, true)

    var itemSelectedListener: IntConsumer? = null

    fun setListenerAndNotify(listener: IntConsumer) {
        itemSelectedListener = listener
        listener.accept(itemSelectedPosition)
    }

    var itemSelectedPosition: Int = AdapterView.INVALID_POSITION
        set(value) {
            if (value == AdapterView.INVALID_POSITION) {
                binding.autoCompleteTextView.setText("", false)
            } else {
                val adapter = binding.autoCompleteTextView.adapter
                if (!adapter.isNullOrEmpty()) {
                    if (value < 0 || value >= adapter.count) {
                        throw IndexOutOfBoundsException("Position out of bounds of dataset")
                    }
                    binding.autoCompleteTextView.setText(adapter.getItem(value).toString(), false)
                }
            }
            itemSelectedListener?.accept(value)
            field = value
        }

    var error: CharSequence?
        get() = binding.textInputLayout.error
        set(value) { binding.textInputLayout.error = value }

    var hint: CharSequence?
        get() = binding.textInputLayout.hint
        set(value) { binding.textInputLayout.hint = value }

    init {
        val attributes = context.theme.obtainStyledAttributes(attributeSet,
            R.styleable.MaterialSpinner, defStyleAttr, defStyleAttr)
        val hint = attributes.getString(R.styleable.MaterialSpinner_msp_hint)
        val spinnerIcon = attributes.getResourceId(R.styleable.MaterialSpinner_msp_icon, 0)
        val iconPosition = attributes.getInt(R.styleable.MaterialSpinner_msp_iconPosition, -1)
        val items = attributes.getResourceId(R.styleable.MaterialSpinner_msp_items, 0)

        when (iconPosition) {
            0 -> setStartIconDrawable(spinnerIcon)
            1 -> setEndIconDrawable(spinnerIcon)
        }
        this.hint = hint
        if (items != 0) {
            setItemStringArrayRes(items)
        }

        attributes.recycle()

        binding.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            itemSelectedPosition = position
            itemSelectedListener?.accept(position)
        }

    }

    /**
     * Set a string array resource id as selections. Can be 0, in which case no items will be set
     * and the [itemSelectedListener] will be called with [AdapterView.INVALID_POSITION].
     */
    fun setItemStringArrayRes(@ArrayRes items: Int) {
        binding.autoCompleteTextView.setAdapter(if (items != 0) ArrayAdapter(context, R.layout.spinner_item, resources.getStringArray(items)) else null)
        selectFirstItemIfAvailableOrNone()
    }

    /**
     * Set an array of objects as selections. In case the array is null or empty, no items will be set
     * and the [itemSelectedListener] will be called with [AdapterView.INVALID_POSITION].
     */
    fun setItemArray(list: Array<Any>?) {
        binding.autoCompleteTextView.setAdapter(if (!list.isNullOrEmpty()) ArrayAdapter(context, R.layout.spinner_item, list) else null)
        selectFirstItemIfAvailableOrNone()
    }

    /**
     * Set a list of objects as selections. In case the list is null or empty, no items will be set
     * and the [itemSelectedListener] will be called with [AdapterView.INVALID_POSITION].
     */
    fun setItemList(list: List<Any>?) {
        binding.autoCompleteTextView.setAdapter(if (!list.isNullOrEmpty()) ArrayAdapter(context, R.layout.spinner_item, list) else null)
        selectFirstItemIfAvailableOrNone()
    }

    /**
     * Set a custom adapter as selections. Can be null, in which case no items will be set
     * and the [itemSelectedListener] will be called with [AdapterView.INVALID_POSITION].
     */
    fun <T> setCustomAdapter(adapter: T?) where T : ListAdapter, T : Filterable {
        binding.autoCompleteTextView.setAdapter(adapter)
        selectFirstItemIfAvailableOrNone()
    }

    private fun selectFirstItemIfAvailableOrNone() {
        val adapter = binding.autoCompleteTextView.adapter
        val textToSet: String
        itemSelectedPosition = if (!adapter.isNullOrEmpty()) {
            textToSet = adapter.getItem(0).toString()
            0
        } else {
            textToSet = ""
            AdapterView.INVALID_POSITION
        }
        binding.autoCompleteTextView.setText(textToSet, false)
        itemSelectedListener?.accept(itemSelectedPosition)
    }

    fun setStartIconDrawable(iconRes: Int) = binding.textInputLayout.setStartIconDrawable(iconRes)

    fun setEndIconDrawable(iconRes: Int) = binding.textInputLayout.setEndIconDrawable(iconRes)

    private fun ListAdapter?.isNullOrEmpty() = this == null || this.count == 0

}