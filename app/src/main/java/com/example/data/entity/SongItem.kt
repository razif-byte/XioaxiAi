package com.example.data.entity

data class SongItem(
    val id: String,
    val title: String,
    val artist: String,
    val description: String,
    val youtubeUrl: String,
    val lyrics: String,
    val shortSingableText: String
)

object SongRepository {
    val songGallery = listOf(
        SongItem(
            id = "dari_bawah",
            title = "Dari Bawah",
            artist = "Ezharhanze",
            description = "Sebuah lagu bertenaga penuh inspirasi tentang perjuangan yang bermula dari bawah demi mencapai kejayaan.",
            youtubeUrl = "https://www.youtube.com/watch?v=Yf1eS61G-6c",
            lyrics = """
                Aku mula dari bawah tak ada siapa pandang,
                Langkah kecil tiap hari walau jalan penuh halang.
                Dulu orang ketawa kata aku takkan ke mana,
                Sekarang aku bangun tunjuk siapa sebenarnya.
                
                Tak pakai cerita kosong semua ni real talk,
                Siang malam aku hustle tak pernah nak stop.
                Tak ada jalan mudah semua kena usaha,
                Jatuh bangun sendiri baru kenal erti maruah.
                
                Dari bawah... aku naik,
                Walau orang... tak nampak.
                Aku cuba... hari-hari,
                Sampai mimpi... jadi nyata.
            """.trimIndent(),
            shortSingableText = "Aku mula dari bawah tak ada siapa pandang, langkah kecil tiap hari walau jalan penuh halang. Dulu orang ketawa kata aku takkan ke mana, sekarang aku bangun tunjuk siapa sebenarnya. Dari bawah aku naik walau orang tak nampak, aku cuba hari-hari sampai mimpi jadi nyata!"
        ),
        SongItem(
            id = "raja",
            title = "Raja",
            artist = "Altimet",
            description = "Lagu yang mengingatkan kita semua agar sentiasa merendah diri, mengenang jasa, dan tidak lupa asal usul.",
            youtubeUrl = "https://www.youtube.com/watch?v=6K_n2M7HhG0",
            lyrics = """
                Dulu datang menumpang teduh,
                Kini menjerit seolah dituduh.
                Lupa akar tempat berpaut,
                Lupa langit tempat tersangkut.
                
                Makan hasil tanah yang subur,
                Minum tenang sungai yang jernih.
                Tapi lidah makin tak terukur,
                Bicara bongkak makin pedih.
                
                Jangan lupa siapa raja,
                Jangan lupa siapa menjaga.
                Kalau tinggi jangan mendada,
                Biar tahu asal beringat.
            """.trimIndent(),
            shortSingableText = "Dulu datang menumpang teduh, kini menjerit seolah dituduh. Lupa akar tempat berpaut, lupa langit tempat tersangkut. Jangan lupa siapa raja, jangan lupa siapa menjaga. Kalau tinggi jangan mendada, biar tahu asal beringat!"
        ),
        SongItem(
            id = "nasihat_diri",
            title = "Nasihat Diri",
            artist = "Malique ft. Kmy Kmo & Luca Sickta",
            description = "Sebuah karya mendalam penuh falsafah tentang masa yang berlalu, peringatan mati, dan amanah kehidupan.",
            youtubeUrl = "https://www.youtube.com/watch?v=gT8PqfG5B_0",
            lyrics = """
                Ini bukan sekadar kata,
                Ini peringatan.
                Yang jauh tu waktu, makin lari makin hilang,
                Jam terus berdetik, hidup makin berkurang.
                
                Nafas ni pinjaman, bukan milik kekal,
                Setiap langkahku sebenarnya makin ke final.
                Aku tengok cermin siapa aku sebenarnya,
                Topeng dunia buat aku lupa daratan.
                
                Yang jauh adalah waktu, aku sia-siakan,
                Yang dekat adalah mati, aku tak endahkan.
                Yang besar itu nafsu, aku bagi makan,
                Yang berat itu amanah, aku tinggalkan.
                Yang berdarah itu dosa...
            """.trimIndent(),
            shortSingableText = "Ini bukan sekadar kata, ini peringatan. Yang jauh tu waktu, makin lari makin hilang. Jam terus berdetik, hidup makin berkurang. Nafas ni pinjaman, bukan milik kekal. Setiap langkahku sebenarnya makin ke final."
        )
    )
}
