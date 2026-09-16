package com.example.tripwise.data

data class CurrencyInfo(
    val country: String,
    val currencyName: String,
    val code: String,
    val symbol: String
) {
    val displayName: String get() = "$country — $currencyName ($code $symbol)"
}

object AsianCurrencies {
    val list = listOf(
        CurrencyInfo("India", "Indian Rupee", "INR", "₹"),
        CurrencyInfo("Japan", "Japanese Yen", "JPY", "¥"),
        CurrencyInfo("South Korea", "South Korean Won", "KRW", "₩"),
        CurrencyInfo("Singapore", "Singapore Dollar", "SGD", "S$"),
        CurrencyInfo("United Arab Emirates", "UAE Dirham", "AED", "د.إ"),
        CurrencyInfo("China", "Chinese Yuan", "CNY", "¥"),
        CurrencyInfo("Thailand", "Thai Baht", "THB", "฿"),
        CurrencyInfo("Indonesia", "Indonesian Rupiah", "IDR", "Rp"),
        CurrencyInfo("Malaysia", "Malaysian Ringgit", "MYR", "RM"),
        CurrencyInfo("Philippines", "Philippine Peso", "PHP", "₱"),
        CurrencyInfo("Vietnam", "Vietnamese Dong", "VND", "₫"),
        CurrencyInfo("Hong Kong", "Hong Kong Dollar", "HKD", "HK$"),
        CurrencyInfo("Taiwan", "New Taiwan Dollar", "TWD", "NT$"),
        CurrencyInfo("Saudi Arabia", "Saudi Riyal", "SAR", "﷼"),
        CurrencyInfo("Qatar", "Qatari Riyal", "QAR", "﷼"),
        CurrencyInfo("Oman", "Omani Rial", "OMR", "﷼"),
        CurrencyInfo("Bahrain", "Bahraini Dinar", "BHD", ".د.ب"),
        CurrencyInfo("Kuwait", "Kuwaiti Dinar", "KWD", "KD"),
        CurrencyInfo("Israel", "Israeli New Shekel", "ILS", "₪"),
        CurrencyInfo("Sri Lanka", "Sri Lankan Rupee", "LKR", "Rs"),
        CurrencyInfo("Nepal", "Nepalese Rupee", "NPR", "रू"),
        CurrencyInfo("Bangladesh", "Bangladeshi Taka", "BDT", "৳"),
        CurrencyInfo("Pakistan", "Pakistani Rupee", "PKR", "₨"),
        CurrencyInfo("Maldives", "Maldivian Rufiyaa", "MVR", "Rf"),
        CurrencyInfo("Turkey", "Turkish Lira", "TRY", "₺"),
        CurrencyInfo("Russia", "Russian Ruble", "RUB", "₽"),
        CurrencyInfo("Kazakhstan", "Kazakhstani Tenge", "KZT", "₸"),
        CurrencyInfo("Uzbekistan", "Uzbekistani Som", "UZS", "so'm"),
        CurrencyInfo("Georgia", "Georgian Lari", "GEL", "₾"),
        CurrencyInfo("Armenia", "Armenian Dram", "AMD", "֏"),
        CurrencyInfo("Azerbaijan", "Azerbaijani Manat", "AZN", "₼"),
        CurrencyInfo("Cambodia", "Cambodian Riel", "KHR", "៛"),
        CurrencyInfo("Laos", "Lao Kip", "LAK", "₭"),
        CurrencyInfo("Mongolia", "Mongolian Tögrög", "MNT", "₮"),
        CurrencyInfo("Myanmar", "Myanmar Kyat", "MMK", "K"),
        CurrencyInfo("Brunei", "Brunei Dollar", "BND", "B$"),
        CurrencyInfo("Bhutan", "Bhutanese Ngultrum", "BTN", "Nu."),
        CurrencyInfo("Iran", "Iranian Rial", "IRR", "﷼"),
        CurrencyInfo("Iraq", "Iraqi Dinar", "IQD", "ع.د"),
        CurrencyInfo("Jordan", "Jordanian Dinar", "JOD", "د.ا"),
        CurrencyInfo("Lebanon", "Lebanese Pound", "LBP", "ل.ل"),
        CurrencyInfo("Syria", "Syrian Pound", "SYP", "£S"),
        CurrencyInfo("Yemen", "Yemeni Rial", "YER", "﷼"),
        CurrencyInfo("Afghanistan", "Afghan Afghani", "AFN", "؋"),
        CurrencyInfo("Kyrgyzstan", "Kyrgyzstani Som", "KGS", "с"),
        CurrencyInfo("Tajikistan", "Tajikistani Somoni", "TJS", "SM"),
        CurrencyInfo("Turkmenistan", "Turkmenistan Manat", "TMT", "T"),
        CurrencyInfo("Macau", "Macanese Pataca", "MOP", "P"),
        CurrencyInfo("North Korea", "North Korean Won", "KPW", "₩")
    )
}
