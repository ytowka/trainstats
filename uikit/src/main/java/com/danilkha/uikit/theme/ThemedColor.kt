package com.danilkha.uikit.theme

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

class ThemedColor private constructor(
    val light: Color,
    val dark: Color,
) : Parcelable {

    @Composable
    @ReadOnlyComposable
    fun get(): Color {
        return if(isSystemInDarkTheme()){
            dark
        }else light
    }

    companion object{
        operator fun invoke(
            provider: (colors: DesignColors) -> Color
        ): ThemedColor{
            return ThemedColor(
                light = provider(lightDesignColors),
                dark = provider(darkDesignColors)
            )
        }

        @JvmField
        val CREATOR = object : Parcelable.Creator<ThemedColor>{
            override fun createFromParcel(source: Parcel?): ThemedColor? {
                return source?.let {
                    val light = Color(source.readLong())
                    val dark = Color(source.readLong())
                    ThemedColor(light, dark)
                }
            }

            override fun newArray(size: Int): Array<ThemedColor?> {
                return arrayOfNulls(size)
            }

        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(light.value.toLong())
        parcel.writeLong(dark.value.toLong())
    }

    override fun describeContents(): Int {
        return 0
    }
}