package com.habitforge.app.data.repository

/**
 * Local quotes database for different languages
 * Since zenquotes.io API doesn't support language selection,
 * we provide pre-translated quotes for supported languages
 */
object LocalQuotes {
    
    private val quotesEn = listOf(
        Quote("The secret of getting ahead is getting started.", "Mark Twain"),
        Quote("Life goes by very fast. And the worst thing in life that you can have is a job that you hate, and have no energy and creativity in.", "Robert Greene"),
        Quote("The only way to do great work is to love what you do.", "Steve Jobs"),
        Quote("Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill"),
        Quote("The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt"),
        Quote("It is during our darkest moments that we must focus to see the light.", "Aristotle"),
        Quote("The way to get started is to quit talking and begin doing.", "Walt Disney"),
        Quote("Don't let yesterday take up too much of today.", "Will Rogers"),
        Quote("You learn more from failure than from success.", "Unknown"),
        Quote("If you are working on something exciting that you really care about, you don't have to be pushed. The vision pulls you.", "Steve Jobs")
    )
    
    private val quotesFr = listOf(
        Quote("Le secret pour avancer est de commencer.", "Mark Twain"),
        Quote("La vie passe très vite. Et la pire chose dans la vie que vous puissiez avoir est un travail que vous détestez, et où vous n'avez ni énergie ni créativité.", "Robert Greene"),
        Quote("La seule façon de faire un excellent travail est d'aimer ce que vous faites.", "Steve Jobs"),
        Quote("Le succès n'est pas final, l'échec n'est pas fatal : c'est le courage de continuer qui compte.", "Winston Churchill"),
        Quote("L'avenir appartient à ceux qui croient en la beauté de leurs rêves.", "Eleanor Roosevelt"),
        Quote("C'est pendant nos moments les plus sombres que nous devons nous concentrer pour voir la lumière.", "Aristote"),
        Quote("La façon de commencer est d'arrêter de parler et de commencer à agir.", "Walt Disney"),
        Quote("Ne laissez pas hier prendre trop de place aujourd'hui.", "Will Rogers"),
        Quote("On apprend plus de l'échec que du succès.", "Inconnu"),
        Quote("Si vous travaillez sur quelque chose d'excitant qui vous tient vraiment à cœur, vous n'avez pas besoin d'être poussé. La vision vous attire.", "Steve Jobs")
    )
    
    private val quotesHi = listOf(
        Quote("आगे बढ़ने का रहस्य शुरू करना है।", "Mark Twain"),
        Quote("जीवन बहुत तेजी से गुजरता है। और जीवन में सबसे बुरी बात यह है कि आपके पास एक ऐसी नौकरी हो जिससे आप नफरत करते हैं, और जिसमें आपके पास ऊर्जा और रचनात्मकता नहीं है।", "Robert Greene"),
        Quote("महान काम करने का एकमात्र तरीका यह है कि आप जो करते हैं उससे प्यार करें।", "Steve Jobs"),
        Quote("सफलता अंतिम नहीं है, असफलता घातक नहीं है: यह जारी रखने का साहस है जो मायने रखता है।", "Winston Churchill"),
        Quote("भविष्य उनका है जो अपने सपनों की सुंदरता में विश्वास करते हैं।", "Eleanor Roosevelt"),
        Quote("यह हमारे सबसे अंधेरे क्षणों के दौरान है कि हमें प्रकाश देखने के लिए ध्यान केंद्रित करना चाहिए।", "अरस्तू"),
        Quote("शुरू करने का तरीका बात करना बंद करना और करना शुरू करना है।", "Walt Disney"),
        Quote("कल को आज में बहुत ज्यादा जगह न दें।", "Will Rogers"),
        Quote("आप सफलता से ज्यादा असफलता से सीखते हैं।", "अज्ञात"),
        Quote("यदि आप किसी रोमांचक चीज़ पर काम कर रहे हैं जिसकी आपको वास्तव में परवाह है, तो आपको धक्का देने की ज़रूरत नहीं है। दृष्टि आपको खींचती है।", "Steve Jobs")
    )
    
    /**
     * Get a quote for today based on language
     * Uses the day of year to select a consistent quote for the day
     */
    fun getTodayQuote(language: String): Quote {
        val quotes = when (language) {
            "fr" -> quotesFr
            "hi" -> quotesHi
            else -> quotesEn
        }
        
        // Use day of year to get a consistent quote for today
        val dayOfYear = java.time.LocalDate.now().dayOfYear
        val index = dayOfYear % quotes.size
        return quotes[index]
    }
    
    /**
     * Get a random quote based on language
     */
    fun getRandomQuote(language: String): Quote {
        val quotes = when (language) {
            "fr" -> quotesFr
            "hi" -> quotesHi
            else -> quotesEn
        }
        return quotes.random()
    }
}
