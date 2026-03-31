package com.example.outofroutebuddy.presentation.ui.settings

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.example.outofroutebuddy.R

/**
 * Preference header with a right-side arrow used for collapsible settings sections.
 * Uses SectionTitle text appearance so headers are visually distinct from child items.
 */
class AccordionHeaderPreference(
    context: Context,
    attrs: AttributeSet? = null,
) : Preference(context, attrs) {

    var isExpanded: Boolean = false
        set(value) {
            field = value
            notifyChanged()
        }

    init {
        isSelectable = true
        isIconSpaceReserved = true
        widgetLayoutResource = R.layout.preference_accordion_widget
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val titleView = holder.findViewById(android.R.id.title) as? TextView
        titleView?.let {
            TextViewCompat.setTextAppearance(it, R.style.TextAppearance_OORB_SectionTitle)
        }

        val iconView = holder.findViewById(android.R.id.icon) as? ImageView
        iconView?.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.settings_pref_icon),
        )

        val arrowView = holder.findViewById(R.id.accordion_arrow) as? ImageView ?: return
        val arrowRes = if (isExpanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
        val arrowDescription = if (isExpanded) "Collapse section" else "Expand section"

        arrowView.setImageResource(arrowRes)
        arrowView.contentDescription = arrowDescription
    }
}
