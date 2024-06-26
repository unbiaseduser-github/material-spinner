package com.sixtyninefourtwenty.materialspinner

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
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
class MaterialSpinner : FrameLayout {

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.MaterialSpinner)
        init(attributes)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.MaterialSpinner, defStyleAttr, 0)
        init(attributes)
    }

    private fun init(attributes: TypedArray) {
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
            _itemSelectedPosition = position
            notifyListeners(position)
        }
    }

    private val binding = MaterialSpinnerContentBinding.inflate(LayoutInflater.from(context), this, true)

    private val listeners = mutableSetOf<IntConsumer>()

    fun addItemSelectedListener(listener: IntConsumer) = listeners.add(listener)

    fun addItemSelectedListenerAndNotify(listener: IntConsumer) {
        val added = addItemSelectedListener(listener)
        if (added) {
            listener.accept(itemSelectedPosition)
        }
    }

    fun removeItemSelectedListener(listener: IntConsumer) = listeners.remove(listener)

    fun clearItemSelectedListeners() = listeners.clear()

    @Deprecated("Use addItemSelectedListener instead.", replaceWith = ReplaceWith("addItemSelectedListener"))
    var itemSelectedListener: IntConsumer? = null

    @Deprecated("Use addItemSelectedListenerAndNotify instead.", replaceWith = ReplaceWith("addItemSelectedListenerAndNotify"))
    fun setListenerAndNotify(listener: IntConsumer) {
        itemSelectedListener = listener
        listener.accept(itemSelectedPosition)
    }

    @Suppress("DEPRECATION")
    private fun notifyListeners(position: Int) {
        listeners.forEach { it.accept(position) }
        itemSelectedListener?.accept(position)
    }

    private var _itemSelectedPosition: Int = AdapterView.INVALID_POSITION

    var itemSelectedPosition: Int
        get() = _itemSelectedPosition
        @SuppressLint("SetTextI18n")
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
            notifyListeners(value)
            _itemSelectedPosition = value
        }

    var error: CharSequence?
        get() = binding.textInputLayout.error
        set(value) { binding.textInputLayout.error = value }

    var hint: CharSequence?
        get() = binding.textInputLayout.hint
        set(value) { binding.textInputLayout.hint = value }

    /**
     * Set a string array resource id as selections. Can be 0, in which case no items will be set
     * and the [itemSelectedListener] will be called with [AdapterView.INVALID_POSITION].
     */
    fun setItemStringArrayRes(@ArrayRes items: Int) {
        setCustomAdapter(if (items != 0) ArrayAdapter(context, R.layout.spinner_item, resources.getStringArray(items)) else null)
    }

    /**
     * Set an array of objects as selections. In case the array is null or empty, no items will be set
     * and the [itemSelectedListener] will be called with [AdapterView.INVALID_POSITION].
     */
    fun setItemArray(list: Array<Any>?) {
        setCustomAdapter(if (!list.isNullOrEmpty()) ArrayAdapter(context, R.layout.spinner_item, list) else null)
    }

    /**
     * Set a list of objects as selections. In case the list is null or empty, no items will be set
     * and the [itemSelectedListener] will be called with [AdapterView.INVALID_POSITION].
     */
    fun setItemList(list: List<Any>?) {
        setCustomAdapter(if (!list.isNullOrEmpty()) ArrayAdapter(context, R.layout.spinner_item, list) else null)
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
        itemSelectedPosition = if (!adapter.isNullOrEmpty()) {
            0
        } else {
            AdapterView.INVALID_POSITION
        }
    }

    fun setStartIconDrawable(iconRes: Int) = binding.textInputLayout.setStartIconDrawable(iconRes)

    fun setEndIconDrawable(iconRes: Int) = binding.textInputLayout.setEndIconDrawable(iconRes)

    private fun ListAdapter?.isNullOrEmpty() = this == null || this.count == 0

}