package com.codidevs.nutriapp.data.models

/** Un alimento con su emoji y nombre (para mostrar la figura). */
data class AlimentoConEmoji(
    val emoji: String,
    val nombre: String,
    val curiosidad: String = ""
)

/** Un grupo alimenticio con su emoji, nombre y los alimentos (con emoji) que lo componen. */
data class GrupoAlimenticio(
    val emoji: String,
    val nombre: String,
    val alimentos: List<AlimentoConEmoji>
)

/**
 * Los 8 grupos de la tabla "Grupo / Alimentos" con beneficios específicos.
 */
object GruposAlimenticios {
    val TODOS = listOf(
        GrupoAlimenticio(
            "🍎", "Frutas",
            listOf(
                AlimentoConEmoji("🍎", "Manzana", "¡La fibra de la manzana ayuda mucho a tu pancita!"),
                AlimentoConEmoji("🍌", "Banano", "¡El banano te da potasio para que tus músculos no se cansen!"),
                AlimentoConEmoji("🍊", "Naranja", "¡La naranja es tu mejor escudo contra los resfriados!"),
                AlimentoConEmoji("🍈", "Papaya", "¡La papaya ayuda a que tu cuerpo digiera mejor todo!"),
                AlimentoConEmoji("🥭", "Mango", "¡El mango cuida tu vista y hace que tu piel brille!"),
                AlimentoConEmoji("🍉", "Sandía", "¡La sandía te hidrata y te llena de frescura!")
            )
        ),
        GrupoAlimenticio(
            "🥦", "Verduras",
            listOf(
                AlimentoConEmoji("🥕", "Zanahoria", "¡La zanahoria te da una super vista de lince!"),
                AlimentoConEmoji("🍅", "Tomate", "¡El tomate mantiene tu corazón fuerte y sano!"),
                AlimentoConEmoji("🥬", "Lechuga", "¡La lechuga es fresca y ayuda a que tu cuerpo esté ligero!"),
                AlimentoConEmoji("🥒", "Pepino", "¡El pepino hidrata cada parte de tu cuerpo!"),
                AlimentoConEmoji("🥗", "Espinaca", "¡La espinaca te da fuerza de superhéroe en los brazos!"),
                AlimentoConEmoji("🥦", "Brócoli", "¡El brócoli tiene calcio para que tus huesos sean fuertes!")
            )
        ),
        GrupoAlimenticio(
            "🍞", "Cereales y harinas",
            listOf(
                AlimentoConEmoji("🍚", "Arroz", "¡El arroz es el combustible para que corras todo el día!"),
                AlimentoConEmoji("🍞", "Pan", "¡El pan le da a tu cerebro energía para aprender mucho!"),
                AlimentoConEmoji("🥔", "Papa", "¡La papa te da energía pura desde la tierra!"),
                AlimentoConEmoji("🍠", "Yuca", "¡La yuca te da resistencia para tus juegos favoritos!"),
                AlimentoConEmoji("🍝", "Pasta", "¡La pasta es la energía preferida de los deportistas!"),
                AlimentoConEmoji("🌾", "Avena", "¡La avena cuida tu corazón y te mantiene satisfecho!"),
                AlimentoConEmoji("🌽", "Maíz", "¡El maíz te mantiene activo y lleno de vitalidad!")
            )
        ),
        GrupoAlimenticio(
            "🍗", "Proteínas",
            listOf(
                AlimentoConEmoji("🍗", "Pollo", "¡El pollo ayuda a construir tus músculos fuertes!"),
                AlimentoConEmoji("🥩", "Carne", "¡La carne tiene hierro para que nunca te sientas cansado!"),
                AlimentoConEmoji("🐟", "Pescado", "¡El pescado ayuda a que tu cerebro sea más inteligente!"),
                AlimentoConEmoji("🥚", "Huevo", "¡El huevo es pura proteína para crecer sano y alto!")
            )
        ),
        GrupoAlimenticio(
            "🫘", "Legumbres",
            listOf(
                AlimentoConEmoji("🫘", "Fríjoles", "¡Los fríjoles tienen fibra para ir muy bien al baño!"),
                AlimentoConEmoji("🍲", "Lentejas", "¡Las lentejas tienen mucho hierro para tu sangre!"),
                AlimentoConEmoji("🌰", "Garbanzos", "¡Los garbanzos te dan mucha energía y fuerza!"),
                AlimentoConEmoji("🫛", "Arvejas", "¡Las arvejas son pequeñas pero poderosas para tu cuerpo!")
            )
        ),
        GrupoAlimenticio(
            "🥛", "Lácteos",
            listOf(
                AlimentoConEmoji("🥛", "Leche", "¡La leche endurece tus huesos como si fueran piedras!"),
                AlimentoConEmoji("🧀", "Queso", "¡El queso ayuda a que tus dientes crezcan muy sanos!"),
                AlimentoConEmoji("🫗", "Yogur", "¡El yogur cuida tu pancita y tus defensas naturales!")
            )
        ),
        GrupoAlimenticio(
            "🥜", "Grasas saludables",
            listOf(
                AlimentoConEmoji("🥑", "Aguacate", "¡El aguacate protege tu corazón de cualquier peligro!"),
                AlimentoConEmoji("🌰", "Nueces", "¡Las nueces son gasolina de calidad para tu cerebro!"),
                AlimentoConEmoji("🥜", "Maní", "¡El maní te da energía duradera para tus aventuras!"),
                AlimentoConEmoji("🫒", "Aceite de oliva", "¡El aceite es como oro líquido para tu salud!")
            )
        ),
        GrupoAlimenticio(
            "🍬", "Azúcares",
            listOf(
                AlimentoConEmoji("🥤", "Gaseosas", "¡El azúcar líquido le quita la fuerza a tu cuerpo!"),
                AlimentoConEmoji("🍬", "Dulces", "¡Mucho azúcar daña tus dientes, come muy poquito!"),
                AlimentoConEmoji("🍫", "Chocolates", "¡El chocolate es rico pero tiene mucha azúcar!"),
                AlimentoConEmoji("🍪", "Galletas", "¡Mejor elige galletas de avena sin azúcar!"),
                AlimentoConEmoji("🍦", "Helados", "¡Son un premio especial, no para comer cada día!")
            )
        )
    )
}
