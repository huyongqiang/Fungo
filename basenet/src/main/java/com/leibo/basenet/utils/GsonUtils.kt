package org.fungo.basenetlib.utils


import android.annotation.SuppressLint
import android.text.TextUtils
import com.google.gson.*
import java.io.Reader
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Author  ZYH
 * Date    2018/1/24
 * Des     GsonUtils
 */
object GsonUtils {
    private val TAG = GsonUtils::class.java.name
    val gson = createGson(true)
    private val GSON_NO_NULLS = createGson(false)

    private fun createGson(serializeNulls: Boolean = true): Gson {
        val builder = GsonBuilder()
        builder.registerTypeAdapter(Date::class.java, DateFormatter())
        builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)

        //Using this naming policy with Gson will ensure that the field name is unchanged.
        //the default type(so,it can omit)
        builder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
        if (serializeNulls) {
            builder.serializeNulls()
        }

        return builder.create()
    }

    fun getGson(serializeNulls: Boolean): Gson {
        return if (serializeNulls) gson else GSON_NO_NULLS
    }

    @JvmOverloads
    fun toJson(`object`: Any, includeNulls: Boolean = true): String {
        return if (includeNulls) gson.toJson(`object`) else GSON_NO_NULLS.toJson(`object`)
    }

    fun <V> fromJson(json: String, type: Class<V>): V {
        return gson.fromJson(json, type)
    }

    fun <V> fromJson(json: String, type: Type): V {
        return gson.fromJson(json, type)
    }

    fun <V> fromJson(params: Map<String, Any>, type: Class<V>): V {
        return gson.fromJson(gson.toJson(params), type)
    }

    fun <V> fromJson(params: Map<String, Any>, type: Type): V? {
        return gson.fromJson<V>(gson.toJson(params), type)
    }

    fun <V> fromJson(reader: Reader, type: Class<V>): V {
        return gson.fromJson(reader, type)
    }

    fun <V> fromJson(reader: Reader, type: Type): V {
        return gson.fromJson(reader, type)
    }

    class DateFormatter @SuppressLint("SimpleDateFormat")
    constructor() : JsonDeserializer<Date>, JsonSerializer<Date> {
        private val formats = arrayOfNulls<DateFormat>(1)

        init {
            this.formats[0] = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val timeZone = TimeZone.getTimeZone("Zulu")
            val var2 = this.formats
            val var3 = var2.size

            (0 until var3)
                    .map { var2[it] }
                    .forEach { it?.timeZone = timeZone }
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Date? {
            val value = json.asString
            if (!TextUtils.isEmpty(value) && value.length != 1) {
                val var5 = this.formats
                val var6 = var5.size
                var var7 = 0

                while (var7 < var6) {
                    val format = var5[var7]
                    if (format != null) {
                        try {
                            synchronized(format) {
                                return format.parse(value)
                            }
                        } catch (var12: ParseException) {
                            android.util.Log.e(TAG, "日期转换错误， " + value, var12)
                            ++var7
                        }
                    }
                }

                return Date(0L)
            } else {
                return null
            }
        }

        override fun serialize(date: Date, type: Type, context: JsonSerializationContext): JsonElement {
            val primary = this.formats[0]
            var formatted: String = ""
            if (primary != null) {
                synchronized(primary) {
                    formatted = primary.format(date)
                }
            }

            return JsonPrimitive(formatted)
        }
    }
}
