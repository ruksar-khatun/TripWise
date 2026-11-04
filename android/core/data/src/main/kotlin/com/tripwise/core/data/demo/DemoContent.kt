package com.tripwise.core.data.demo

import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.Destination
import com.tripwise.core.model.GeoPoint
import com.tripwise.core.model.ImageAttribution
import com.tripwise.core.model.ImageRef
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.model.TravelInterest.ADVENTURE
import com.tripwise.core.model.TravelInterest.BEACHES
import com.tripwise.core.model.TravelInterest.CITY
import com.tripwise.core.model.TravelInterest.CULTURE
import com.tripwise.core.model.TravelInterest.FOOD
import com.tripwise.core.model.TravelInterest.MOUNTAINS
import com.tripwise.core.model.TravelInterest.NATURE

/**
 * DEMO CATALOG: curated destinations until the backend catalog phase replaces this.
 * Photos are Unsplash (free Unsplash License); see scripts/check-demo-images.sh, run in CI.
 */
internal object DemoContent {

    private fun unsplash(id: String) = ImageRef(
        url = "https://images.unsplash.com/photo-$id?auto=format&fit=crop&w=1200&q=75",
        attribution = ImageAttribution(author = "Unsplash", sourceUrl = "https://unsplash.com"),
    )

    @Suppress("LongParameterList")
    private fun d(
        id: String, name: String, country: String, region: String, photo: String,
        lat: Double, lng: Double, interests: Set<TravelInterest>, rating: Double, ratingCount: Int,
        best: String, currency: String, languages: List<String>,
        summary: String, highlights: List<String>, food: List<String>,
    ) = Destination(
        id = id, name = name, country = country, region = region, summary = summary,
        highlights = highlights, localFood = food, images = listOf(unsplash(photo)),
        location = GeoPoint(lat, lng), interests = interests, rating = rating, ratingCount = ratingCount,
        bestTimeToVisit = best, currency = CurrencyCode(currency), languages = languages,
    )

    val destinations: List<Destination> = listOf(
        d("bali", "Bali", "Indonesia", "Southeast Asia", "1537996194471-e657df975ab4", -8.34, 115.09,
            setOf(BEACHES, CULTURE, NATURE), 4.7, 2140, "Apr – Oct", "IDR", listOf("Indonesian", "Balinese"),
            "Volcanic highlands, terraced rice fields and surf beaches, with a living Hindu culture woven through daily life.",
            listOf("Sunrise over Mount Batur", "Tegallalang rice terraces", "Uluwatu cliff temple at sunset", "Ubud's craft villages"),
            listOf("Babi guling", "Nasi campur", "Sate lilit", "Lawar")),
        d("kyoto", "Kyoto", "Japan", "East Asia", "1493976040374-85c8e12f0c0e", 35.01, 135.77,
            setOf(CULTURE, FOOD, CITY), 4.8, 3320, "Mar – May, Oct – Nov", "JPY", listOf("Japanese"),
            "Japan's former capital: more than a thousand temples, wooden machiya streets and an exceptional food culture.",
            listOf("Fushimi Inari's torii gates", "Arashiyama bamboo grove", "Gion at dusk", "Kiyomizu-dera"),
            listOf("Kaiseki", "Yudofu", "Matcha sweets", "Nishiki Market street food")),
        d("tokyo", "Tokyo", "Japan", "East Asia", "1540959733332-eab4deabeeaf", 35.68, 139.69,
            setOf(CITY, FOOD, CULTURE), 4.7, 5410, "Mar – May, Sep – Nov", "JPY", listOf("Japanese"),
            "A vast, orderly megacity where neon districts sit beside quiet shrines and some of the world's best restaurants.",
            listOf("Shibuya Crossing", "Senso-ji in Asakusa", "teamLab digital art", "Day trip to Mount Fuji"),
            listOf("Sushi", "Ramen", "Tempura", "Monjayaki")),
        d("seoul", "Seoul", "South Korea", "East Asia", "1538485399081-7191377e8241", 37.57, 126.98,
            setOf(CITY, FOOD, CULTURE), 4.6, 2870, "Apr – Jun, Sep – Nov", "KRW", listOf("Korean"),
            "Palaces and hanok villages framed by mountains, with round-the-clock markets and a fast-moving pop culture.",
            listOf("Gyeongbokgung Palace", "Bukchon Hanok Village", "Myeongdong night market", "N Seoul Tower"),
            listOf("Bibimbap", "Korean BBQ", "Tteokbokki", "Kimchi jjigae")),
        d("bangkok", "Bangkok", "Thailand", "Southeast Asia", "1508009603885-50cf7c579365", 13.76, 100.50,
            setOf(CITY, FOOD, CULTURE), 4.5, 4020, "Nov – Feb", "THB", listOf("Thai"),
            "Gilded temples, floating markets and a legendary street-food scene along the Chao Phraya river.",
            listOf("Wat Arun at sunset", "Grand Palace", "Chatuchak weekend market", "Long-tail boat canals"),
            listOf("Pad thai", "Tom yum goong", "Mango sticky rice", "Boat noodles")),
        d("singapore", "Singapore", "Singapore", "Southeast Asia", "1525625293386-3f8f99389edd", 1.35, 103.82,
            setOf(CITY, FOOD), 4.6, 3650, "Feb – Apr", "SGD", listOf("English", "Malay", "Mandarin", "Tamil"),
            "A green, spotless city-state where hawker centres, futuristic gardens and four cultures meet.",
            listOf("Gardens by the Bay", "Marina Bay skyline", "Hawker centres", "Sentosa island"),
            listOf("Hainanese chicken rice", "Chilli crab", "Laksa", "Kaya toast")),
        d("dubai", "Dubai", "United Arab Emirates", "Middle East", "1512453979798-5ea266f8880c", 25.20, 55.27,
            setOf(CITY, ADVENTURE), 4.5, 2980, "Nov – Mar", "AED", listOf("Arabic", "English"),
            "Record-breaking skyscrapers, desert dunes and old souks along the creek.",
            listOf("Burj Khalifa observation deck", "Desert safari", "Gold and spice souks", "Dubai Marina"),
            listOf("Al machboos", "Shawarma", "Luqaimat", "Camel-milk chocolate")),
        d("agra", "Agra", "India", "South Asia", "1564507592333-c60657eea523", 27.18, 78.01,
            setOf(CULTURE), 4.6, 2210, "Oct – Mar", "INR", listOf("Hindi"),
            "Home of the Taj Mahal and a cluster of Mughal masterpieces on the banks of the Yamuna.",
            listOf("Taj Mahal at sunrise", "Agra Fort", "Mehtab Bagh", "Fatehpur Sikri"),
            listOf("Petha", "Bedai with aloo sabzi", "Mughlai biryani", "Dalmoth")),
        d("jaipur", "Jaipur", "India", "South Asia", "1477587458883-47145ed94245", 26.91, 75.79,
            setOf(CULTURE, FOOD), 4.5, 1890, "Oct – Mar", "INR", listOf("Hindi", "Rajasthani"),
            "The Pink City: hilltop forts, palace courtyards and bazaars full of block prints and gems.",
            listOf("Amber Fort", "Hawa Mahal", "City Palace", "Johari Bazaar"),
            listOf("Dal baati churma", "Laal maas", "Pyaaz kachori", "Ghevar")),
        d("kerala", "Kerala Backwaters", "India", "South Asia", "1602216056096-3b40cc0c9944", 9.50, 76.34,
            setOf(NATURE, FOOD), 4.6, 1540, "Sep – Mar", "INR", listOf("Malayalam"),
            "A slow network of palm-lined lagoons and canals, best seen from a traditional houseboat.",
            listOf("Overnight houseboat in Alleppey", "Kumarakom bird sanctuary", "Village canoe rides", "Kathakali performance"),
            listOf("Karimeen pollichathu", "Appam with stew", "Sadya", "Puttu and kadala")),
        d("halong", "Ha Long Bay", "Vietnam", "Southeast Asia", "1528127269322-539801943592", 20.91, 107.18,
            setOf(NATURE, ADVENTURE), 4.6, 1760, "Mar – May, Sep – Nov", "VND", listOf("Vietnamese"),
            "Thousands of limestone islands rising from emerald water, explored by junk boat and kayak.",
            listOf("Overnight junk cruise", "Kayaking through caves", "Sung Sot cave", "Ti Top island viewpoint"),
            listOf("Cha muc (squid cakes)", "Pho", "Fresh seafood", "Banh cuon")),
        d("maldives", "Maldives", "Maldives", "South Asia", "1514282401047-d79a71a590e8", 3.20, 73.22,
            setOf(BEACHES, NATURE), 4.8, 2650, "Nov – Apr", "MVR", listOf("Dhivehi", "English"),
            "Coral atolls with overwater villas, clear lagoons and some of the world's best snorkelling.",
            listOf("House reef snorkelling", "Sandbank picnics", "Bioluminescent beaches", "Manta ray dives"),
            listOf("Mas huni", "Garudhiya", "Hedhikaa snacks", "Bis keemiya")),
        d("santorini", "Santorini", "Greece", "Europe", "1570077188670-e3a8d69ac5ff", 36.39, 25.46,
            setOf(BEACHES, CULTURE), 4.7, 3010, "Apr – Jun, Sep – Oct", "EUR", listOf("Greek"),
            "White villages on volcanic cliffs above a flooded caldera, famous for its sunsets and wines.",
            listOf("Sunset in Oia", "Caldera boat trip", "Red Beach", "Akrotiri excavations"),
            listOf("Tomatokeftedes", "Fava", "Fresh grilled fish", "Assyrtiko wine")),
        d("paris", "Paris", "France", "Europe", "1502602898657-3e91760cbb34", 48.86, 2.35,
            setOf(CITY, CULTURE, FOOD), 4.7, 6120, "Apr – Jun, Sep – Oct", "EUR", listOf("French"),
            "Grand boulevards, world-class museums and café terraces along the Seine.",
            listOf("The Louvre", "Eiffel Tower at night", "Montmartre", "Seine river cruise"),
            listOf("Croissants", "Steak frites", "Macarons", "Crêpes")),
        d("swiss-alps", "Swiss Alps", "Switzerland", "Europe", "1530122037265-a5f1f91d3b99", 46.56, 7.98,
            setOf(MOUNTAINS, NATURE, ADVENTURE), 4.9, 2390, "Jun – Sep, Dec – Mar", "CHF", listOf("German", "French", "Italian"),
            "Glaciers, alpine lakes and scenic railways, with hiking in summer and skiing in winter.",
            listOf("Jungfraujoch", "Matterhorn views from Zermatt", "Glacier Express", "Lake Brienz"),
            listOf("Fondue", "Raclette", "Rösti", "Swiss chocolate")),
        d("cappadocia", "Cappadocia", "Türkiye", "Middle East", "1641128324972-af3212f0f6bd", 38.64, 34.83,
            setOf(ADVENTURE, CULTURE, NATURE), 4.8, 1980, "Apr – Jun, Sep – Nov", "TRY", listOf("Turkish"),
            "Fairy-chimney valleys and cave towns, best seen from a hot-air balloon at dawn.",
            listOf("Sunrise balloon flight", "Göreme Open-Air Museum", "Derinkuyu underground city", "Rose Valley hike"),
            listOf("Testi kebab", "Manti", "Gözleme", "Turkish delight")),
    )
}
