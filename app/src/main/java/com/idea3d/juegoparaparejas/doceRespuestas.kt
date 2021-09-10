package com.idea3d.juegoparaparejas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.idea3d.juegoparaparejas.databinding.ActivityDoceRespuestasBinding
import java.util.Timer
import kotlin.concurrent.schedule
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


class doceRespuestas : AppCompatActivity() {
    private lateinit var binding: ActivityDoceRespuestasBinding
    private var interstitial: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoceRespuestasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initIntAds()
        initLoadAds()


        var i=1
        val respuestasDos:ArrayList<Int?> = arrayListOf<Int?>()
        var comparaResp=0
        var promedio=0.0

        val jug1 = intent.getStringExtra("jugador1")
        val jug2 = intent.getStringExtra("jugador2")
        val prueba = intent.getIntExtra("prueba", 0)
        val respuestas = intent.getIntegerArrayListExtra("respuestas")


        fun pruebaUno(i:Int){
            when(i) {
                1-> {binding.preguntaView.text="Tu estación favorita es..."
                    binding.respuestaUno.text= "OTOÑO"
                    binding.respuestaDos.text= "INVIERNO"
                    binding.respuestaTres.text= "PRIMAVERA"
                    binding.respuestaCuatro.text= "VERANO"}


                2-> {binding.preguntaView.text="Si tu vida fuera un video juego ¿de que tipo sería?"
                    binding.respuestaUno.text= "Acción"
                    binding.respuestaDos.text= "Aventura"
                    binding.respuestaTres.text= "Deportes"
                    binding.respuestaCuatro.text= "Fiesta"}

                3-> {binding.preguntaView.text="¿Cual es tu tipo de música preferida?"
                    binding.respuestaUno.text= "Pop"
                    binding.respuestaDos.text= "Rock"
                    binding.respuestaTres.text= "Reggaeton"
                    binding.respuestaCuatro.text= "Electrónica"}

                4-> {binding.preguntaView.text="¿A qué animal te pareces más?"
                    binding.respuestaUno.text= "Perro"
                    binding.respuestaDos.text= "Gato"
                    binding.respuestaTres.text= "León"
                    binding.respuestaCuatro.text= "Tortuga"}

                5-> {binding.preguntaView.text="Es viernes en la noche y tú..."
                    binding.respuestaUno.text= "¡De fiesta!"
                    binding.respuestaDos.text= "Trabajando..."
                    binding.respuestaTres.text= "Durmiendo"
                    binding.respuestaCuatro.text= "viendo series o películas"}

                6-> {binding.preguntaView.text="Si tuvieras un deseo ¿Cual sería?"
                    binding.respuestaUno.text= "Ser el mejor en todo"
                    binding.respuestaDos.text= "Superpoderes"
                    binding.respuestaTres.text= "Dinero ¡y mucho!"
                    binding.respuestaCuatro.text= "Dominar el mundo"}

                7-> {binding.preguntaView.text="En qué época debiste haber nacido"
                    binding.respuestaUno.text= "La edad oscura"
                    binding.respuestaDos.text= "Los 60s"
                    binding.respuestaTres.text= "La actualidad"
                    binding.respuestaCuatro.text= "El futuro"}

                8-> {binding.preguntaView.text="¿Qué te haría más feliz?"
                    binding.respuestaUno.text= "Dormir más"
                    binding.respuestaDos.text= "Menos trabajo"
                    binding.respuestaTres.text= "Más tiempo con amigos y familia"
                    binding.respuestaCuatro.text= "Más dinero"}

                9-> {binding.preguntaView.text="Describe tu estilo en una palabra"
                    binding.respuestaUno.text= "Fino"
                    binding.respuestaDos.text= "Glamuroso"
                    binding.respuestaTres.text= "Elegante"
                    binding.respuestaCuatro.text= "Hipster"}

                10-> {binding.preguntaView.text="¿Que buscas más en un amigo?"
                    binding.respuestaUno.text= "Lealtad"
                    binding.respuestaDos.text= "Amabilidad"
                    binding.respuestaTres.text= "Humor"
                    binding.respuestaCuatro.text= "Inteligencia"}

                11-> {binding.preguntaView.text="Tu color favorito es..."
                    binding.respuestaUno.text= "Verde"
                    binding.respuestaDos.text= "Rojo/Rosa"
                    binding.respuestaTres.text= "Azul/Violeta"
                    binding.respuestaCuatro.text= "Amarillo"}

                12-> {binding.preguntaView.text="¿Cual es tu tipo de película favorita"
                    binding.respuestaUno.text= "Acción"
                    binding.respuestaDos.text= "Comedia"
                    binding.respuestaTres.text= "Romance"
                    binding.respuestaCuatro.text= "Ciencia Ficción"}


            }
        }
        fun pruebaDos(i:Int){
            when(i) {
                1-> {binding.preguntaView.text="¿Cuál es tu deporte favorito"
                    binding.respuestaUno.text= "Béisbol"
                    binding.respuestaDos.text= "Fútbol"
                    binding.respuestaTres.text= "Basket"
                    binding.respuestaCuatro.text= "Tennis"}


                2-> {binding.preguntaView.text="¿Cuál es la cosa más importante en tu vida"
                    binding.respuestaUno.text= "Familia"
                    binding.respuestaDos.text= "Amigos"
                    binding.respuestaTres.text= "Trabajo"
                    binding.respuestaCuatro.text= "Pasatiempos"}

                3-> {binding.preguntaView.text="¿Cual es tu bebida favorita?"
                    binding.respuestaUno.text= "Agua"
                    binding.respuestaDos.text= "Café/Té"
                    binding.respuestaTres.text= "Gaseosa"
                    binding.respuestaCuatro.text= "Alcohol"}

                4-> {binding.preguntaView.text="¿A qué le tienes más miedo?"
                    binding.respuestaUno.text= "Insectos"
                    binding.respuestaDos.text= "Alturas"
                    binding.respuestaTres.text= "Encierro"
                    binding.respuestaCuatro.text= "Fracaso"}

                5-> {binding.preguntaView.text="¿Qué prefieres para el desayuno?"
                    binding.respuestaUno.text= "Tostadas"
                    binding.respuestaDos.text= "Cereal"
                    binding.respuestaTres.text= "Huevos"
                    binding.respuestaCuatro.text= "Sólo un café"}

                6-> {binding.preguntaView.text="¿A donde irías en un día libre?"
                    binding.respuestaUno.text= "El campo"
                    binding.respuestaDos.text= "La ciudad"
                    binding.respuestaTres.text= "La playa"
                    binding.respuestaCuatro.text= "Me quedaría en casa"}

                7-> {binding.preguntaView.text="¿Cual es tu momento favorito del día"
                    binding.respuestaUno.text= "La mañana"
                    binding.respuestaDos.text= "La tarde"
                    binding.respuestaTres.text= "La noche"
                    binding.respuestaCuatro.text= "La madrugada"}

                8-> {binding.preguntaView.text="¿Qué llevas siempre en tu bolso de viaje?"
                    binding.respuestaUno.text= "Un cargador de teléfono"
                    binding.respuestaDos.text= "Dinero extra"
                    binding.respuestaTres.text= "Una guía de viaje"
                    binding.respuestaCuatro.text= "Un cepillo de dientes"}

                9-> {binding.preguntaView.text="Qué superpoder te gustaría tener"
                    binding.respuestaUno.text= "Superfuerza"
                    binding.respuestaDos.text= "Ser invisible"
                    binding.respuestaTres.text= "Detener el tiempo"
                    binding.respuestaCuatro.text= "Volar"}

                10-> {binding.preguntaView.text="¿Que te gustaría ser?"
                    binding.respuestaUno.text= "Un músico famoso"
                    binding.respuestaDos.text= "Un atleta de élite"
                    binding.respuestaTres.text= "Un político respetado"
                    binding.respuestaCuatro.text= "Un gran profesional"}

                11-> {binding.preguntaView.text="Si sólo quedarán dos horas para el fin del mundo ¿Qué harías?"
                    binding.respuestaUno.text= "Dormir"
                    binding.respuestaDos.text= "Buscar una buena vista"
                    binding.respuestaTres.text= "Cualquier locura que se me ocurra"
                    binding.respuestaCuatro.text= "Decirle adiós a mis seres queridos"}

                12-> {
                    "¿Qué preferirías recibir cómo regalo".also { binding.preguntaView.text = it }
                    binding.respuestaUno.text= "Chocolates"
                    binding.respuestaDos.text= "Libros"
                    binding.respuestaTres.text= "Flores"
                    binding.respuestaCuatro.text= "Alcohol"}


            }
        }
        fun pruebaTres(i:Int){
            when(i) {

                1 -> {
                    "¿Qué te llevarías a una isla desierta".also { binding.preguntaView.text = it }
                    "Un libro".also { binding.respuestaUno.text = it }
                    "Una radio".also { binding.respuestaDos.text = it }
                    "Un cuchillo".also { binding.respuestaTres.text = it }
                    "Un balón".also { binding.respuestaCuatro.text = it }
                }
                2 -> {
                    "¿Qué prefieres cómo tema de conversación?".also { binding.preguntaView.text = it }
                    "Deportes".also { binding.respuestaUno.text = it }
                    "Música".also { binding.respuestaDos.text = it }
                    "Películas y series".also { binding.respuestaTres.text = it }
                    "Política".also { binding.respuestaCuatro.text = it }
                }
                3 -> {
                    "¿Qué prefieres hacer un fin de semana?".also { binding.preguntaView.text = it }
                    "Pescar".also { binding.respuestaUno.text = it }
                    "Escalar montañas".also { binding.respuestaDos.text = it }
                    "Broncearte".also { binding.respuestaTres.text = it }
                    "Ir a conciertos".also { binding.respuestaCuatro.text = it }
                }
                4 -> {
                    "Una película sobre tu vida ¿Qué tipo de película sería?".also { binding.preguntaView.text = it }
                    "Una comedia".also { binding.respuestaUno.text = it }
                    "Un drama".also { binding.respuestaDos.text = it }
                    "De Acción".also { binding.respuestaTres.text = it }
                    "Romántica".also { binding.respuestaCuatro.text = it }
                }
                5 -> {
                    "¿Cuál de tus sentidos te gustaría mejorar?".also { binding.preguntaView.text = it }
                    "Vista".also { binding.respuestaUno.text = it }
                    "Audición".also { binding.respuestaDos.text = it }
                    "Olfato".also { binding.respuestaTres.text = it }
                    "Gusto".also { binding.respuestaCuatro.text = it }
                }
                6 -> {
                    "¿Qué adjetivo te define mejor?".also { binding.preguntaView.text = it }
                    "Infantil".also { binding.respuestaUno.text = it }
                    "Alegre".also { binding.respuestaDos.text = it }
                    "Sociable".also { binding.respuestaTres.text = it }
                    "Trabajador".also { binding.respuestaCuatro.text = it }
                }
                7 -> {
                    "A qué lección preferirías inscribirte?".also { binding.preguntaView.text = it }
                    "Tiro con arco".also { binding.respuestaUno.text = it }
                    "Cerámica".also { binding.respuestaDos.text = it }
                    "Esgrima".also { binding.respuestaTres.text = it }
                    "Baile".also { binding.respuestaCuatro.text = it }
                }
                8 -> {
                    "¿Sin qué no podrías vivir?".also { binding.preguntaView.text = it }
                    "Música".also { binding.respuestaUno.text = it }
                    "Teléfono móvil".also { binding.respuestaDos.text = it }
                    "Mi mascota".also { binding.respuestaTres.text = it }
                    "Agua caliente".also { binding.respuestaCuatro.text = it }
                }

                9 -> {
                    "¿Qué harías si ganaras la loteria".also { binding.preguntaView.text = it }
                    "Comprar un coche nuevo".also { binding.respuestaUno.text = it }
                    "Invertir el dinero".also { binding.respuestaDos.text = it }
                    "Comprar una casa".also { binding.respuestaTres.text = it }
                    "Ayudar a los necesitados".also { binding.respuestaCuatro.text = it }
                }
                10 -> {
                    "Qué harías con 2 horas de tiempo libre".also { binding.preguntaView.text = it }
                    "Dormir".also { binding.respuestaUno.text = it }
                    "Estudiar".also { binding.respuestaDos.text = it }
                    "Entrar en mis redes sociales".also { binding.respuestaTres.text = it }
                    "Jugar videojuegos".also { binding.respuestaCuatro.text = it }
                }
                11 -> {
                    "¿Qué animal preferirías cómo mascota?".also { binding.preguntaView.text = it }
                    "Perro".also { binding.respuestaUno.text = it }
                    "Gato".also { binding.respuestaDos.text = it }
                    "Serpiente".also { binding.respuestaTres.text = it }
                    "Caballo".also { binding.respuestaCuatro.text = it }
                }
                12 -> {
                    "Cuál es el sueño de tu vida?".also { binding.preguntaView.text = it }
                    "Ser millonario".also { binding.respuestaUno.text = it }
                    "Ser famoso".also { binding.respuestaDos.text = it }
                    "Tener muchos hijos".also { binding.respuestaTres.text = it }
                    "Viajar por el mundo".also { binding.respuestaCuatro.text = it }
                }
            }

        }
        fun pruebaCuatro(i:Int){
            when(i) {

                1 -> {
                    "¿Qué te atrae más?".also { binding.preguntaView.text = it }
                    "Dinero".also { binding.respuestaUno.text = it }
                    "Familia".also { binding.respuestaDos.text = it }
                    "Estabilidad".also { binding.respuestaTres.text = it }
                    "Aventura".also { binding.respuestaCuatro.text = it }
                }
                2 -> {
                    "¿Qué preferirías ser?".also { binding.preguntaView.text = it }
                    "Más fuerte".also { binding.respuestaUno.text = it }
                    "Más alto".also { binding.respuestaDos.text = it }
                    "Más bajo".also { binding.respuestaTres.text = it }
                    "Más delgado".also { binding.respuestaCuatro.text = it }
                }
                3 -> {
                    "¿Qué papel te gustaría en una obra de teatro?".also { binding.preguntaView.text = it }
                    "El héroe".also { binding.respuestaUno.text = it }
                    "El amigo del héroe".also { binding.respuestaDos.text = it }
                    "El villano".also { binding.respuestaTres.text = it }
                    "Un árbol".also { binding.respuestaCuatro.text = it }
                }
                4 -> {
                    "A que preferirías poner fin?".also { binding.preguntaView.text = it }
                    "La Guerra".also { binding.respuestaUno.text = it }
                    "El hambre".also { binding.respuestaDos.text = it }
                    "Enfermedades".also { binding.respuestaTres.text = it }
                    "Contaminación".also { binding.respuestaCuatro.text = it }
                }
                5 -> {
                    "¿Qué actividad prefieres".also { binding.preguntaView.text = it }
                    "Jugar un videojuego".also { binding.respuestaUno.text = it }
                    "Ver la televisión".also { binding.respuestaDos.text = it }
                    "Hacer ejercicio".also { binding.respuestaTres.text = it }
                    "Jugar un juego de mesa".also { binding.respuestaCuatro.text = it }
                }
                6 -> {
                    "¿Qué prefieres ver en la televisión?".also { binding.preguntaView.text = it }
                    "Deportes".also { binding.respuestaUno.text = it }
                    "Noticias".also { binding.respuestaDos.text = it }
                    "Documentales".also { binding.respuestaTres.text = it }
                    "Reality Shows".also { binding.respuestaCuatro.text = it }
                }
                7 -> {
                    "Dondes prefieres pasar tus vacaciones".also { binding.preguntaView.text = it }
                    "En el caribe".also { binding.respuestaUno.text = it }
                    "En las montañas".also { binding.respuestaDos.text = it }
                    "En una gran ciudad".also { binding.respuestaTres.text = it }
                    "En un safarí".also { binding.respuestaCuatro.text = it }
                }
                8 -> {
                    "Cual es tu medio de transporte favorito".also { binding.preguntaView.text = it }
                    "Tren".also { binding.respuestaUno.text = it }
                    "Barco".also { binding.respuestaDos.text = it }
                    "Coche".also { binding.respuestaTres.text = it }
                    "Avión".also { binding.respuestaCuatro.text = it }
                }

                9 -> {
                    "¿Qué tan inteligente eres?".also { binding.preguntaView.text = it }
                    "Brillante".also { binding.respuestaUno.text = it }
                    "Casi un genio".also { binding.respuestaDos.text = it }
                    "Promedio".also { binding.respuestaTres.text = it }
                    "Algo Tonto".also { binding.respuestaCuatro.text = it }
                }
                10 -> {
                    "¿Cuál es tu asignatura favorita?".also { binding.preguntaView.text = it }
                    "Matemática".also { binding.respuestaUno.text = it }
                    "Ciencia".also { binding.respuestaDos.text = it }
                    "Educación Física".also { binding.respuestaTres.text = it }
                    "Arte".also { binding.respuestaCuatro.text = it }
                }
                11 -> {
                    "Cuando es tu cumpleaños?".also { binding.preguntaView.text = it }
                    "Verano".also { binding.respuestaUno.text = it }
                    "Otoño".also { binding.respuestaDos.text = it }
                    "Primavera".also { binding.respuestaTres.text = it }
                    "Invierno".also { binding.respuestaCuatro.text = it }
                }
                12 -> {
                    "¿Cuál es tu animal favorito?".also { binding.preguntaView.text = it }
                    "Perro".also { binding.respuestaUno.text = it }
                    "Gato".also { binding.respuestaDos.text = it }
                    "Caballo".also { binding.respuestaTres.text = it }
                    "Conejo".also { binding.respuestaCuatro.text = it }
                }
            }

        }
        fun pruebaCinco(i:Int){
            when(i) {

                1 -> {
                    "¿Cuál es tu elemento favorito?".also { binding.preguntaView.text = it }
                    "Fuego".also { binding.respuestaUno.text = it }
                    "Aire".also { binding.respuestaDos.text = it }
                    "Tierra".also { binding.respuestaTres.text = it }
                    "Agua".also { binding.respuestaCuatro.text = it }
                }
                2 -> {
                    "¿Cuando eres más productivo?".also { binding.preguntaView.text = it }
                    "Mañana".also { binding.respuestaUno.text = it }
                    "Tarde".also { binding.respuestaDos.text = it }
                    "Noche".also { binding.respuestaTres.text = it }
                    "Madrugada".also { binding.respuestaCuatro.text = it }
                }
                3 -> {
                    "¿Quién eres en tu grupo de amigos?".also { binding.preguntaView.text = it }
                    "El lider".also { binding.respuestaUno.text = it }
                    "El chistoso".also { binding.respuestaDos.text = it }
                    "El creativo".also { binding.respuestaTres.text = it }
                    "El Leal".also { binding.respuestaCuatro.text = it }
                }
                4 -> {
                    "¿Cuál es tu fecha favorita?".also { binding.preguntaView.text = it }
                    "Navidad".also { binding.respuestaUno.text = it }
                    "Pascuas".also { binding.respuestaDos.text = it }
                    "San Valentin".also { binding.respuestaTres.text = it }
                    "Halloween".also { binding.respuestaCuatro.text = it }
                }
                5 -> {
                    "¿Cuál es el trabajo de tus sueños?".also { binding.preguntaView.text = it }
                    "Médico".also { binding.respuestaUno.text = it }
                    "Músico".also { binding.respuestaDos.text = it }
                    "Atleta".also { binding.respuestaTres.text = it }
                    "Astronauta".also { binding.respuestaCuatro.text = it }
                }
                6 -> {
                    "¿En qué desearías ser mejor?".also { binding.preguntaView.text = it }
                    "Matemáticas".also { binding.respuestaUno.text = it }
                    "Canto".also { binding.respuestaDos.text = it }
                    "Oratoria".also { binding.respuestaTres.text = it }
                    "Deportes".also { binding.respuestaCuatro.text = it }
                }
                7 -> {
                    "¿De qué te gustaría tener un año de suministro?".also { binding.preguntaView.text = it }
                    "Cereal".also { binding.respuestaUno.text = it }
                    "Helado".also { binding.respuestaDos.text = it }
                    "Donas".also { binding.respuestaTres.text = it }
                    "Chocolate".also { binding.respuestaCuatro.text = it }
                }
                8 -> {
                    "¿Qué actividad preferirías intentar? ".also { binding.preguntaView.text = it }
                    "Ir a un safarí".also { binding.respuestaUno.text = it }
                    "Tirarte en paracaídas".also { binding.respuestaDos.text = it }
                    "Escalar un volcán".also { binding.respuestaTres.text = it }
                    "Nadar con delfines".also { binding.respuestaCuatro.text = it }
                }

                9 -> {
                    "¿Cual es tu atividad favorita alrededor de una fogata?".also { binding.preguntaView.text = it }
                    "Cantar".also { binding.respuestaUno.text = it }
                    "Contar historias de terror".also { binding.respuestaDos.text = it }
                    "Asar malvaviscos".also { binding.respuestaTres.text = it }
                    "Jugar charadas".also { binding.respuestaCuatro.text = it }
                }
                10 -> {
                    "¿Qué te gusta más de la navidad?".also { binding.preguntaView.text = it }
                    "Estar con tu familia".also { binding.respuestaUno.text = it }
                    "La fiesta".also { binding.respuestaDos.text = it }
                    "Comer y beber".also { binding.respuestaTres.text = it }
                    "Los regalos".also { binding.respuestaCuatro.text = it }
                }
                11 -> {
                    "¿Que prefieres jugar?".also { binding.preguntaView.text = it }
                    "Videojuegos".also { binding.respuestaUno.text = it }
                    "Juegos de mesa".also { binding.respuestaDos.text = it }
                    "Cartas".also { binding.respuestaTres.text = it }
                    "Deportes".also { binding.respuestaCuatro.text = it }
                }
                12 -> {
                    "¿Cómo sería tu salida nocturna ideal?".also { binding.preguntaView.text = it }
                    "Ver un partido de fútbol".also { binding.respuestaUno.text = it }
                    "Ir al teatro".also { binding.respuestaDos.text = it }
                    "Ir a un concierto".also { binding.respuestaTres.text = it }
                    "Ir a un club".also { binding.respuestaCuatro.text = it }
                }
            }

        }
        fun pruebaSeis(i:Int){
            when(i) {

                1 -> {
                    "¿Qué habilidad extraordinria te gustaría tener?".also { binding.preguntaView.text = it }
                    "Volar".also { binding.respuestaUno.text = it }
                    "Respirar bajo el agua".also { binding.respuestaDos.text = it }
                    "Caminar por las paredes".also { binding.respuestaTres.text = it }
                    "Visión de rayos X".also { binding.respuestaCuatro.text = it }
                }
                2 -> {
                    "¿A quién te gustaría tener de amigo?".also { binding.preguntaView.text = it }
                    "Mi estrella de cine favorita".also { binding.respuestaUno.text = it }
                    "Mi músico favorito".also { binding.respuestaDos.text = it }
                    "Mi deportista favorito".also { binding.respuestaTres.text = it }
                    "Mi político favorito".also { binding.respuestaCuatro.text = it }
                }
                3 -> {
                    "¿Qué papel te gustaría en una película?".also { binding.preguntaView.text = it }
                    "Un apuesto héroe".also { binding.respuestaUno.text = it }
                    "Un maligno villano".also { binding.respuestaDos.text = it }
                    "El creativo que se roba la escena".also { binding.respuestaTres.text = it }
                    "El mejor amigo del héroe".also { binding.respuestaCuatro.text = it }
                }
                4 -> {
                    "¿Cómo serían tus vacaciones ideales?".also { binding.preguntaView.text = it }
                    "De relax en la playa".also { binding.respuestaUno.text = it }
                    "En una gran ciudad con muchos museos".also { binding.respuestaDos.text = it }
                    "De fiesta en fiesta".also { binding.respuestaTres.text = it }
                    "De senderismo en las montañas".also { binding.respuestaCuatro.text = it }
                }
                5 -> {
                    "¿Cuál de estos países te gustaría visitar más?".also { binding.preguntaView.text = it }
                    "Perú".also { binding.respuestaUno.text = it }
                    "Japón".also { binding.respuestaDos.text = it }
                    "Francia".also { binding.respuestaTres.text = it }
                    "EEUU".also { binding.respuestaCuatro.text = it }
                }
                6 -> {
                    "Cuál es tu red social favorita?".also { binding.preguntaView.text = it }
                    "Instagram".also { binding.respuestaUno.text = it }
                    "Tik Tok".also { binding.respuestaDos.text = it }
                    "Twitter".also { binding.respuestaTres.text = it }
                    "Facebook".also { binding.respuestaCuatro.text = it }
                }
                7 -> {
                    "¿Qué tipo de bocadillo prefieres?".also { binding.preguntaView.text = it }
                    "Chocolate".also { binding.respuestaUno.text = it }
                    "Patatas fritas".also { binding.respuestaDos.text = it }
                    "Galletas".also { binding.respuestaTres.text = it }
                    "Fruta".also { binding.respuestaCuatro.text = it }
                }
                8 -> {
                    "¿Qué harías si fueras invisible? ".also { binding.preguntaView.text = it }
                    "Hacer bromas".also { binding.respuestaUno.text = it }
                    "Robar dinero".also { binding.respuestaDos.text = it }
                    "Espiar a la gente".also { binding.respuestaTres.text = it }
                    "Hacer buenas cosas para el mundo".also { binding.respuestaCuatro.text = it }
                }

                9 -> {
                    "Si tuvieras que contratar a alguien ¿A qué cualidad le darías mayor valor?".also { binding.preguntaView.text = it }
                    "Apariencia".also { binding.respuestaUno.text = it }
                    "Experiencia".also { binding.respuestaDos.text = it }
                    "Educación".also { binding.respuestaTres.text = it }
                    "Carisma".also { binding.respuestaCuatro.text = it }
                }
                10 -> {
                    "¿Qué situación sería la peor para ti?".also { binding.preguntaView.text = it }
                    "Apocalipsis Zombie".also { binding.respuestaUno.text = it }
                    "Rebelión de las máquinas".also { binding.respuestaDos.text = it }
                    "Invasión extraterrestre".also { binding.respuestaTres.text = it }
                    "Una gran inundación".also { binding.respuestaCuatro.text = it }
                }
                11 -> {
                    "¿Cómo sería tu domingo perfecto?".also { binding.preguntaView.text = it }
                    "Jugar juegos de mesa".also { binding.respuestaUno.text = it }
                    "Un viaje por la carretera".also { binding.respuestaDos.text = it }
                    "Beber en bares".also { binding.respuestaTres.text = it }
                    "Una gran fiesta".also { binding.respuestaCuatro.text = it }
                }
                12 -> {
                    "¿Qué haces para relajarte?".also { binding.preguntaView.text = it }
                    "Tomar sol en el jardín".also { binding.respuestaUno.text = it }
                    "Tirarme en el sofá".also { binding.respuestaDos.text = it }
                    "Beber con amigos".also { binding.respuestaTres.text = it }
                    "Jugar videojuegos".also { binding.respuestaCuatro.text = it }
                }
            }

        }
        fun pruebaSiete(i:Int){
            when(i) {

                1 -> {
                    "¿Cuál es tu comida favorita?".also { binding.preguntaView.text = it }
                    "Sushi".also { binding.respuestaUno.text = it }
                    "Pasta".also { binding.respuestaDos.text = it }
                    "Pizza".also { binding.respuestaTres.text = it }
                    "Hamburguesas".also { binding.respuestaCuatro.text = it }
                }
                2 -> {
                    "¿Que ambiente natural es tu favorito?".also { binding.preguntaView.text = it }
                    "El bosque".also { binding.respuestaUno.text = it }
                    "Montañas nevadas".also { binding.respuestaDos.text = it }
                    "La playa".also { binding.respuestaTres.text = it }
                    "Un lago y montañas".also { binding.respuestaCuatro.text = it }
                }
                3 -> {
                    "¿Qué te motiva más?".also { binding.preguntaView.text = it }
                    "El éxito".also { binding.respuestaUno.text = it }
                    "La familia".also { binding.respuestaDos.text = it }
                    "El poder".also { binding.respuestaTres.text = it }
                    "La competencia".also { binding.respuestaCuatro.text = it }
                }
                4 -> {
                    "¿Que instrumento te gustaría poder tocar a la perfección?".also { binding.preguntaView.text = it }
                    "Guitarra".also { binding.respuestaUno.text = it }
                    "Bajo".also { binding.respuestaDos.text = it }
                    "Bateria".also { binding.respuestaTres.text = it }
                    "Piano".also { binding.respuestaCuatro.text = it }
                }
                5 -> {
                    "¿Cuál es tu día favorito de la semana?".also { binding.preguntaView.text = it }
                    "Sábado".also { binding.respuestaUno.text = it }
                    "Domingo".also { binding.respuestaDos.text = it }
                    "Viernes".also { binding.respuestaTres.text = it }
                    "Otro".also { binding.respuestaCuatro.text = it }
                }
                6 -> {
                    "Cuál es tu comida favorita?".also { binding.preguntaView.text = it }
                    "Desayuno".also { binding.respuestaUno.text = it }
                    "Almuerzo".also { binding.respuestaDos.text = it }
                    "Merienda".also { binding.respuestaTres.text = it }
                    "Cena".also { binding.respuestaCuatro.text = it }
                }
                7 -> {
                    "¿Qué género de tv en tu favorito?".also { binding.preguntaView.text = it }
                    "Drama".also { binding.respuestaUno.text = it }
                    "Comedia".also { binding.respuestaDos.text = it }
                    "Crimen".also { binding.respuestaTres.text = it }
                    "Deporte".also { binding.respuestaCuatro.text = it }
                }
                8 -> {
                    "¿Cuál crees que es tu cualidad más atractiva?".also { binding.preguntaView.text = it }
                    "Apariencia".also { binding.respuestaUno.text = it }
                    "Inteligencia".also { binding.respuestaDos.text = it }
                    "Ambición".also { binding.respuestaTres.text = it }
                    "Humor".also { binding.respuestaCuatro.text = it }
                }

                9 -> {
                    "¿Qué deberías mejorar?".also { binding.preguntaView.text = it }
                    "Tu dieta".also { binding.respuestaUno.text = it }
                    "Carrera profesional".also { binding.respuestaDos.text = it }
                    "Finanzas".also { binding.respuestaTres.text = it }
                    "Cultura".also { binding.respuestaCuatro.text = it }
                }
                10 -> {
                    "¿Qué superhéroe te gustaría ser?".also { binding.preguntaView.text = it }
                    "Batman".also { binding.respuestaUno.text = it }
                    "Superman".also { binding.respuestaDos.text = it }
                    "Spiderman".also { binding.respuestaTres.text = it }
                    "La mujer maravilla".also { binding.respuestaCuatro.text = it }
                }
                11 -> {
                    "¿Cómo sería tu primera cita perfecta?".also { binding.preguntaView.text = it }
                    "Cenar en casa".also { binding.respuestaUno.text = it }
                    "Bailar y beber en un club".also { binding.respuestaDos.text = it }
                    "Ir a un concierto".also { binding.respuestaTres.text = it }
                    "Ir al cine".also { binding.respuestaCuatro.text = it }
                }
                12 -> {
                    "¿Qué tan seguido haces ejercicio?".also { binding.preguntaView.text = it }
                    "Todos los días".also { binding.respuestaUno.text = it }
                    "5 veces a la semana".also { binding.respuestaDos.text = it }
                    "2 o 3 veces por semana".also { binding.respuestaTres.text = it }
                    "Una vez o nunca".also { binding.respuestaCuatro.text = it }
                }
            }

        }
        fun pruebaOcho(i:Int){
            when(i) {

                1 -> {
                    "¿Cómo prefieres pasar una hora de tiempo libre?".also { binding.preguntaView.text = it }
                    "Yendo de compras".also { binding.respuestaUno.text = it }
                    "Durmiendo".also { binding.respuestaDos.text = it }
                    "En el gimnasio".also { binding.respuestaTres.text = it }
                    "Viendo TV".also { binding.respuestaCuatro.text = it }
                }
                2 -> {
                    "¿A quién elegirías para cambiar de vida por un año?".also { binding.preguntaView.text = it }
                    "Tu jefe".also { binding.respuestaUno.text = it }
                    "Tu hermano o hermana".also { binding.respuestaDos.text = it }
                    "Un amigo".also { binding.respuestaTres.text = it }
                    "Tu pareja".also { binding.respuestaCuatro.text = it }
                }
                3 -> {
                    "Por lo general ¿Qué sueles usar más a menudo?".also { binding.preguntaView.text = it }
                    "El corazón".also { binding.respuestaUno.text = it }
                    "La lógica".also { binding.respuestaDos.text = it }
                    "El dinero".also { binding.respuestaTres.text = it }
                    "La experiencia".also { binding.respuestaCuatro.text = it }
                }
                4 -> {
                    "¿Que te daría mayor felicidad?".also { binding.preguntaView.text = it }
                    "Viajar por el mundo".also { binding.respuestaUno.text = it }
                    "Hacer más amigos".also { binding.respuestaDos.text = it }
                    "Tener más dinero".also { binding.respuestaTres.text = it }
                    "Estar en mejor forma".also { binding.respuestaCuatro.text = it }
                }
                5 -> {
                    "¿Cuál es tu mayor prioridad ahora mismo?".also { binding.preguntaView.text = it }
                    "Relación sentimental".also { binding.respuestaUno.text = it }
                    "Carrera profesional".also { binding.respuestaDos.text = it }
                    "Reputación".also { binding.respuestaTres.text = it }
                    "Éxito".also { binding.respuestaCuatro.text = it }
                }
                6 -> {
                    "Cuál es tu miedo más grande?".also { binding.preguntaView.text = it }
                    "La muerte".also { binding.respuestaUno.text = it }
                    "Estar solo".also { binding.respuestaDos.text = it }
                    "Los bichos".also { binding.respuestaTres.text = it }
                    "Espacios pequeños".also { binding.respuestaCuatro.text = it }
                }
                7 -> {
                    "¿Cómo calificarías tu forma de bailar?".also { binding.preguntaView.text = it }
                    "Desastrosa".also { binding.respuestaUno.text = it }
                    "Mala".also { binding.respuestaDos.text = it }
                    "Promedio".also { binding.respuestaTres.text = it }
                    "Genial".also { binding.respuestaCuatro.text = it }
                }
                8 -> {
                    "¿Qué te gustaría más?".also { binding.preguntaView.text = it }
                    "Ser más alto".also { binding.respuestaUno.text = it }
                    "Ser más delgado".also { binding.respuestaDos.text = it }
                    "Ser más jóven".also { binding.respuestaTres.text = it }
                    "Ser más bajo".also { binding.respuestaCuatro.text = it }
                }

                9 -> {
                    "¿Qué preferirías hacer?".also { binding.preguntaView.text = it }
                    "Manejar un moto de agua".also { binding.respuestaUno.text = it }
                    "Bucear".also { binding.respuestaDos.text = it }
                    "Saltar en paracaídas".also { binding.respuestaTres.text = it }
                    "Pilotear un avión".also { binding.respuestaCuatro.text = it }
                }
                10 -> {
                    "¿Cómo calificarías tu forma de cantar?".also { binding.preguntaView.text = it }
                    "Excelente".also { binding.respuestaUno.text = it }
                    "Promedio".also { binding.respuestaDos.text = it }
                    "Mala".also { binding.respuestaTres.text = it }
                    "Desastrosa".also { binding.respuestaCuatro.text = it }
                }
                11 -> {
                    "¿Qué priorizas en tus vacaciones?".also { binding.preguntaView.text = it }
                    "La comida".also { binding.respuestaUno.text = it }
                    "El alojamiento".also { binding.respuestaDos.text = it }
                    "Espectáculos".also { binding.respuestaTres.text = it }
                    "La compania".also { binding.respuestaCuatro.text = it }
                }
                12 -> {
                    "¿En qué eres peor?".also { binding.preguntaView.text = it }
                    "Bailando".also { binding.respuestaUno.text = it }
                    "Cantando".also { binding.respuestaDos.text = it }
                    "Dibujando".also { binding.respuestaTres.text = it }
                    "Deportes".also { binding.respuestaCuatro.text = it }
                }
            }

        }
        fun pruebaNueve(i:Int){
            when(i) {

                1 -> {
                    "¿Qué preferirías tener en tu casa?".also { binding.preguntaView.text = it }
                    "Una sala de juegos".also { binding.respuestaUno.text = it }
                    "Un gimnasio".also { binding.respuestaDos.text = it }
                    "Un estudio".also { binding.respuestaTres.text = it }
                    "Una piscina".also { binding.respuestaCuatro.text = it }
                }
                2 -> {
                    "¿Cómo calificarías tu puntualidad?".also { binding.preguntaView.text = it }
                    "Excelente".also { binding.respuestaUno.text = it }
                    "Buena".also { binding.respuestaDos.text = it }
                    "Promedio".also { binding.respuestaTres.text = it }
                    "Muy mala".also { binding.respuestaCuatro.text = it }
                }
                3 -> {
                    "¿Cómo serían unas vaciones de pesadilla?".also { binding.preguntaView.text = it }
                    "Caminando mucho".also { binding.respuestaUno.text = it }
                    "En un campamento".also { binding.respuestaDos.text = it }
                    "En la playa".also { binding.respuestaTres.text = it }
                    "En una ciudad".also { binding.respuestaCuatro.text = it }
                }
                4 -> {
                    "¿Cuál es tu género literario favorito?".also { binding.preguntaView.text = it }
                    "Romance".also { binding.respuestaUno.text = it }
                    "Crimen".also { binding.respuestaDos.text = it }
                    "Historia".also { binding.respuestaTres.text = it }
                    "Biográficos".also { binding.respuestaCuatro.text = it }
                }
                5 -> {
                    "¿Qué empleo se ajusta mejor a tu perfil?".also { binding.preguntaView.text = it }
                    "Doctor".also { binding.respuestaUno.text = it }
                    "Maestro".also { binding.respuestaDos.text = it }
                    "Abogado".also { binding.respuestaTres.text = it }
                    "Vendedor".also { binding.respuestaCuatro.text = it }
                }
                6 -> {
                    "¿Cómo eres en la cocina?".also { binding.preguntaView.text = it }
                    "Todo un chef".also { binding.respuestaUno.text = it }
                    "Nada mal".also { binding.respuestaDos.text = it }
                    "Todo se quema".also { binding.respuestaTres.text = it }
                    "No cocinas".also { binding.respuestaCuatro.text = it }
                }
                7 -> {
                    "Elige una de las siguientes mascotas".also { binding.preguntaView.text = it }
                    "Una cobra".also { binding.respuestaUno.text = it }
                    "Un chimpancé".also { binding.respuestaDos.text = it }
                    "Un hámster".also { binding.respuestaTres.text = it }
                    "Un elefante".also { binding.respuestaCuatro.text = it }
                }
                8 -> {
                    "¿Qué monumento te gustaría visitar?".also { binding.preguntaView.text = it }
                    "La estatua de la libertad".also { binding.respuestaUno.text = it }
                    "La gran muralla china".also { binding.respuestaDos.text = it }
                    "La torre Eiffel".also { binding.respuestaTres.text = it }
                    "Las pirámides de Egipto".also { binding.respuestaCuatro.text = it }
                }

                9 -> {
                    "¿Qué utilizas más a menudo?".also { binding.preguntaView.text = it }
                    "Netflix".also { binding.respuestaUno.text = it }
                    "Amazon Prime".also { binding.respuestaDos.text = it }
                    "Disney+".also { binding.respuestaTres.text = it }
                    "Youtube".also { binding.respuestaCuatro.text = it }
                }
                10 -> {
                    "¿Qué tipo de clima prefieres?".also { binding.preguntaView.text = it }
                    "Soleado".also { binding.respuestaUno.text = it }
                    "Lluvioso".also { binding.respuestaDos.text = it }
                    "Nevado".also { binding.respuestaTres.text = it }
                    "Ventoso".also { binding.respuestaCuatro.text = it }
                }
                11 -> {
                    "¿Qué prefieres para tarjeta de regalo?".also { binding.preguntaView.text = it }
                    "Comida".also { binding.respuestaUno.text = it }
                    "Viajes".also { binding.respuestaDos.text = it }
                    "Cosméticos".also { binding.respuestaTres.text = it }
                    "Videojuegos".also { binding.respuestaCuatro.text = it }
                }
                12 -> {
                    "¿Cuál piensas que es tu mayor fortaleza?".also { binding.preguntaView.text = it }
                    "Buen humor".also { binding.respuestaUno.text = it }
                    "Lealtad".also { binding.respuestaDos.text = it }
                    "Ambición".also { binding.respuestaTres.text = it }
                    "Calidez".also { binding.respuestaCuatro.text = it }
                }
            }

        }
        fun pruebaDiez(i:Int){
            when(i) {

                1 -> {
                    "Tienes que renunciar a una cosa por todo un mes ¿Cuál sería?".also { binding.preguntaView.text = it }
                    "Teléfono móvil".also { binding.respuestaUno.text = it }
                    "Redes sociales".also { binding.respuestaDos.text = it }
                    "Videojuegos".also { binding.respuestaTres.text = it }
                    "Chocolate".also { binding.respuestaCuatro.text = it }
                }
                2 -> {
                    "¿Cuál es tu mayor preocupación?".also { binding.preguntaView.text = it }
                    "Salud".also { binding.respuestaUno.text = it }
                    "Trabajo".also { binding.respuestaDos.text = it }
                    "Dinero".also { binding.respuestaTres.text = it }
                    "Familia".also { binding.respuestaCuatro.text = it }
                }
                3 -> {
                    "¿En donde disfrutas más?".also { binding.preguntaView.text = it }
                    "En una boda".also { binding.respuestaUno.text = it }
                    "En un cumpleaños".also { binding.respuestaDos.text = it }
                    "En Navidad".also { binding.respuestaTres.text = it }
                    "En Año Nuevo".also { binding.respuestaCuatro.text = it }
                }
                4 -> {
                    "¿Cuántos libros lees al mes?".also { binding.preguntaView.text = it }
                    "0".also { binding.respuestaUno.text = it }
                    "1".also { binding.respuestaDos.text = it }
                    "2".also { binding.respuestaTres.text = it }
                    "Más de tres".also { binding.respuestaCuatro.text = it }
                }
                5 -> {
                    "Si te hicieran una fiesta ¿Qué sentirías?".also { binding.preguntaView.text = it }
                    "Emoción".also { binding.respuestaUno.text = it }
                    "Agradecimiento".also { binding.respuestaDos.text = it }
                    "Verguenza".also { binding.respuestaTres.text = it }
                    "Euforia".also { binding.respuestaCuatro.text = it }
                }
                6 -> {
                    "¿Qué preferirías tener en tu casa?".also { binding.preguntaView.text = it }
                    "Un trampolín".also { binding.respuestaUno.text = it }
                    "Una piscina olímpica".also { binding.respuestaDos.text = it }
                    "Una casita del árbol".also { binding.respuestaTres.text = it }
                    "Una cancja de tennis".also { binding.respuestaCuatro.text = it }
                }
                7 -> {
                    "¿Quién es la primera persona a la que cuentas una buena noticia?".also { binding.preguntaView.text = it }
                    "Tus padres".also { binding.respuestaUno.text = it }
                    "Tu mejor amigo".also { binding.respuestaDos.text = it }
                    "Tu pareja".also { binding.respuestaTres.text = it }
                    "Tu hermano".also { binding.respuestaCuatro.text = it }
                }
                8 -> {
                    "¿Qué extravías más a menudo?".also { binding.preguntaView.text = it }
                    "Teléfono móvil".also { binding.respuestaUno.text = it }
                    "Cartera".also { binding.respuestaDos.text = it }
                    "Anteojos".also { binding.respuestaTres.text = it }
                    "Llaves".also { binding.respuestaCuatro.text = it }
                }

                9 -> {
                    "¿Qué te parece más divertido?".also { binding.preguntaView.text = it }
                    "Una caminata tranquila".also { binding.respuestaUno.text = it }
                    "Salir a bailar".also { binding.respuestaDos.text = it }
                    "Tomar un café con un amigo".also { binding.respuestaTres.text = it }
                    "Hacer deportes".also { binding.respuestaCuatro.text = it }
                }
                10 -> {
                    "¿Qúe factor es más importante a la hora de comprar algo?".also { binding.preguntaView.text = it }
                    "La calidad".also { binding.respuestaUno.text = it }
                    "El precio".also { binding.respuestaDos.text = it }
                    "La moda".also { binding.respuestaTres.text = it }
                    "Lo práctico".also { binding.respuestaCuatro.text = it }
                }
                11 -> {
                    "¿Cuál de las siguientes cosas ansías más?".also { binding.preguntaView.text = it }
                    "Conocimientos".also { binding.respuestaUno.text = it }
                    "Dinero".also { binding.respuestaDos.text = it }
                    "Comida".also { binding.respuestaTres.text = it }
                    "Tiempo de relax".also { binding.respuestaCuatro.text = it }
                }
                12 -> {
                    "Si tuvieras que dedicar tiempo a una sola de estas cosas por el resto de tu vida ¿Cuál eliges?".also { binding.preguntaView.text = it }
                    "Familia".also { binding.respuestaUno.text = it }
                    "Amigos".also { binding.respuestaDos.text = it }
                    "Trabajo".also { binding.respuestaTres.text = it }
                    "Pasatiempos".also { binding.respuestaCuatro.text = it }
                }
            }

        }
        fun pruebaOnce(i:Int){
            when(i) {

                1 -> {
                    "¿Con cuántas personas tuviste sexo?".also { binding.preguntaView.text = it }
                    "1-3".also { binding.respuestaUno.text = it }
                    "3-10".also { binding.respuestaDos.text = it }
                    "10-20".also { binding.respuestaTres.text = it }
                    "20+".also { binding.respuestaCuatro.text = it }
                }
                2 -> {
                    "¿Jugaste Strip Poker alguna vez?".also { binding.preguntaView.text = it }
                    "Sí y lo volvería a hacer".also { binding.respuestaUno.text = it }
                    "No pero quiero hacerlo".also { binding.respuestaDos.text = it }
                    "Si pero no quiero que se repita".also { binding.respuestaTres.text = it }
                    "No lo haría jamás".also { binding.respuestaCuatro.text = it }
                }
                3 -> {
                    "¿Cual es el lugar más incómodo donde lo has hecho?".also { binding.preguntaView.text = it }
                    "En un baño".also { binding.respuestaUno.text = it }
                    "En la playa".also { binding.respuestaDos.text = it }
                    "En un auto".also { binding.respuestaTres.text = it }
                    "En la calle".also { binding.respuestaCuatro.text = it }
                }
                4 -> {
                    "Si te proponen grabar un video xxx, tú:".also { binding.preguntaView.text = it }
                    "Te enojas".also { binding.respuestaUno.text = it }
                    "Te excitas".also { binding.respuestaDos.text = it }
                    "Te ríes".also { binding.respuestaTres.text = it }
                    "Te averguenzas".also { binding.respuestaCuatro.text = it }
                }
                5 -> {
                    "Si sólo pudieras tocarme en una parte del cuerpo ¿Cuál sería?".also { binding.preguntaView.text = it }
                    "La cara".also { binding.respuestaUno.text = it }
                    "El pecho".also { binding.respuestaDos.text = it }
                    "Las piernas".also { binding.respuestaTres.text = it }
                    "Partes íntimas".also { binding.respuestaCuatro.text = it }
                }
                6 -> {
                    "¿Cuánto tiempo has estado sin acostarte con nadie?".also { binding.preguntaView.text = it }
                    "Una semana".also { binding.respuestaUno.text = it }
                    "Un mes".also { binding.respuestaDos.text = it }
                    "Seis meses".also { binding.respuestaTres.text = it }
                    "Más de un año".also { binding.respuestaCuatro.text = it }
                }
                7 -> {
                    "¿Harías un trío?".also { binding.preguntaView.text = it }
                    "Sí, con otra chica".also { binding.respuestaUno.text = it }
                    "Sí, con otro chico".also { binding.respuestaDos.text = it }
                    "No ahora, quizás más adelante".also { binding.respuestaTres.text = it }
                    "Jamás".also { binding.respuestaCuatro.text = it }
                }
                8 -> {
                    "¿Qué es lo que más te excita?".also { binding.preguntaView.text = it }
                    "Un buen cuerpo".also { binding.respuestaUno.text = it }
                    "La inteligencia".also { binding.respuestaDos.text = it }
                    "El atrevimiento".also { binding.respuestaTres.text = it }
                    "La bondad".also { binding.respuestaCuatro.text = it }
                }

                9 -> {
                    "¿Qué te parece más divertido?".also { binding.preguntaView.text = it }
                    "Una caminata tranquila".also { binding.respuestaUno.text = it }
                    "Salir a bailar".also { binding.respuestaDos.text = it }
                    "Tomar un café con un amigo".also { binding.respuestaTres.text = it }
                    "Hacer deportes".also { binding.respuestaCuatro.text = it }
                }
                10 -> {
                    "¿A qué todavía no te animas?".also { binding.preguntaView.text = it }
                    "Sadomasoquismo".also { binding.respuestaUno.text = it }
                    "Sexo anal".also { binding.respuestaDos.text = it }
                    "Un trío".also { binding.respuestaTres.text = it }
                    "Disfraces".also { binding.respuestaCuatro.text = it }
                }
                11 -> {
                    "¿En que lugar público te gustaría hacerlo?".also { binding.preguntaView.text = it }
                    "Calle".also { binding.respuestaUno.text = it }
                    "Una plaza".also { binding.respuestaDos.text = it }
                    "La playa".also { binding.respuestaTres.text = it }
                    "Un bosque".also { binding.respuestaCuatro.text = it }
                }
                12 -> {
                    "¿Alguna vez tuviste un sueño erótico con alguna persona que no debías?".also { binding.preguntaView.text = it }
                    "Un amigo/a".also { binding.respuestaUno.text = it }
                    "Un/a familiar".also { binding.respuestaDos.text = it }
                    "Un/a compañero/a".also { binding.respuestaTres.text = it }
                    "Nunca".also { binding.respuestaCuatro.text = it }
                }
            }

        }
        fun pruebaDoce(i:Int){
            when(i) {

                1 -> {
                    "¿Cuál fue el máximo de personas con las que has tenido sexo al mismo tiempo?".also { binding.preguntaView.text = it }
                    "Nunca con más de una".also { binding.respuestaUno.text = it }
                    "Dos".also { binding.respuestaDos.text = it }
                    "Tres".also { binding.respuestaTres.text = it }
                    "Más de tres".also { binding.respuestaCuatro.text = it }
                }
                2 -> {
                    "¿Cuánto tiempo puedes estar haciendólo sin parar?".also { binding.preguntaView.text = it }
                    "Cinco minutos".also { binding.respuestaUno.text = it }
                    "Treinta minutos".also { binding.respuestaDos.text = it }
                    "Una hora".also { binding.respuestaTres.text = it }
                    "Más de dos horas".also { binding.respuestaCuatro.text = it }
                }
                3 -> {
                    "¿Alguna vez estuviste en un club swingers?".also { binding.preguntaView.text = it }
                    "Sí y volvería a hacerlo".also { binding.respuestaUno.text = it }
                    "Sí pero no me gustó".also { binding.respuestaDos.text = it }
                    "No pero me da curiosidad".also { binding.respuestaTres.text = it }
                    "Jamás lo haría".also { binding.respuestaCuatro.text = it }
                }
                4 -> {
                    "¿Qué personaje ficticio te consideras debajo de las sábanas?".also { binding.preguntaView.text = it }
                    "Ironman".also { binding.respuestaUno.text = it }
                    "La mujer maravilla".also { binding.respuestaDos.text = it }
                    "La bella durmiente".also { binding.respuestaTres.text = it }
                    "Drácula".also { binding.respuestaCuatro.text = it }
                }
                5 -> {
                    "¿Cómo te gusta que te hagan sexo oral?".also { binding.preguntaView.text = it }
                    "Con cariño".also { binding.respuestaUno.text = it }
                    "Bien guarro".also { binding.respuestaDos.text = it }
                    "Extremo".also { binding.respuestaTres.text = it }
                    "No me gusta".also { binding.respuestaCuatro.text = it }
                }
                6 -> {
                    "¿Qué es lo más lejos a lo que has llegado en una primera cita?".also { binding.preguntaView.text = it }
                    "Un beso francés".also { binding.respuestaUno.text = it }
                    "Sexo oral".also { binding.respuestaDos.text = it }
                    "Sexo tradicional".also { binding.respuestaTres.text = it }
                    "Sexo con otras personas".also { binding.respuestaCuatro.text = it }
                }
                7 -> {
                    "¿Cual es tu especialidad?".also { binding.preguntaView.text = it }
                    "Los disfraces".also { binding.respuestaUno.text = it }
                    "Los juguetes".also { binding.respuestaDos.text = it }
                    "La previa y el sexo oral".also { binding.respuestaTres.text = it }
                    "Tu forma de moverte en la cama".also { binding.respuestaCuatro.text = it }
                }
                8 -> {
                    "Si se vienen en tu boca, tú:".also { binding.preguntaView.text = it }
                    "Escupes".also { binding.respuestaUno.text = it }
                    "Tragas".also { binding.respuestaDos.text = it }
                    "Pides más".also { binding.respuestaTres.text = it }
                    "Te enojas".also { binding.respuestaCuatro.text = it }
                }

                9 -> {
                    "¿De qué parte de tu cuerpo estás más orgulloso/a?".also { binding.preguntaView.text = it }
                    "Tu carita".also { binding.respuestaUno.text = it }
                    "Tu colita".also { binding.respuestaDos.text = it }
                    "Tu pecho".also { binding.respuestaTres.text = it }
                    "Tu cosita".also { binding.respuestaCuatro.text = it }
                }
                10 -> {
                    "¿Qué es lo más embarazoso que te ha pasado estando con alguien?".also { binding.preguntaView.text = it }
                    "No funcionó el amigo".also { binding.respuestaUno.text = it }
                    "Alguien se vino demasiado rápido".also { binding.respuestaDos.text = it }
                    "Nos encontraron".also { binding.respuestaTres.text = it }
                    "Hubiera sido mejor tener un inodoro cerca".also { binding.respuestaCuatro.text = it }
                }
                11 -> {
                    "¿Alguna fantasia que no hayas cumplido?".also { binding.preguntaView.text = it }
                    "En un avión".also { binding.respuestaUno.text = it }
                    "En un lugar público".also { binding.respuestaDos.text = it }
                    "En un auto en movimiento".also { binding.respuestaTres.text = it }
                    "Juego de roles con disfraces".also { binding.respuestaCuatro.text = it }
                }
                12 -> {
                    "¿Con qué frecuencia te gusta tener sexo?".also { binding.preguntaView.text = it }
                    "Todos los días".also { binding.respuestaUno.text = it }
                    "Día por medio".also { binding.respuestaDos.text = it }
                    "Una vez por semana".also { binding.respuestaTres.text = it }
                    "Una vez al mes".also { binding.respuestaCuatro.text = it }
                }
            }

        }
        fun pruebaTrece(i:Int){
            when(i) {

                1 -> {
                    "¿Qué posición te gusta más?".also { binding.preguntaView.text = it }
                    "Misionero".also { binding.respuestaUno.text = it }
                    "De perrito".also { binding.respuestaDos.text = it }
                    "A cococho".also { binding.respuestaTres.text = it }
                    "Boca abajo".also { binding.respuestaCuatro.text = it }
                }
                2 -> {
                    "¿Cual es tu manera de seducir?".also { binding.preguntaView.text = it }
                    "Halagando a la otra persona".also { binding.respuestaUno.text = it }
                    "Hablando de tus proezas".also { binding.respuestaDos.text = it }
                    "Haciendo reir a la otra persona".also { binding.respuestaTres.text = it }
                    "Con una mirada penetrante".also { binding.respuestaCuatro.text = it }
                }
                3 -> {
                    "¿Te has acostado con una ex pareja después de cortar?".also { binding.preguntaView.text = it }
                    "Sí, al poco tiempo".also { binding.respuestaUno.text = it }
                    "Sí, un reencuentro por los viejos tiempos".also { binding.respuestaDos.text = it }
                    "No pero nos hemos contactado".also { binding.respuestaTres.text = it }
                    "Jamás lo haría".also { binding.respuestaCuatro.text = it }
                }
                4 -> {
                    "¿Que clase de juguetes te gusta?".also { binding.preguntaView.text = it }
                    "Animalitos".also { binding.respuestaUno.text = it }
                    "Collares".also { binding.respuestaDos.text = it }
                    "Cosas que me hagan doler y vibrar".also { binding.respuestaTres.text = it }
                    "Ninguna".also { binding.respuestaCuatro.text = it }
                }
                5 -> {
                    "¿Qué clase de disfraz es tu favorito?".also { binding.preguntaView.text = it }
                    "Policia".also { binding.respuestaUno.text = it }
                    "Anime".also { binding.respuestaDos.text = it }
                    "Colegiala".also { binding.respuestaTres.text = it }
                    "Superhéroes".also { binding.respuestaCuatro.text = it }
                }
                6 -> {
                    "¿Cómo te gustaría que se vistiera tu pareja para dormir contigo?".also { binding.preguntaView.text = it }
                    "Encaje".also { binding.respuestaUno.text = it }
                    "Camiseta larga".also { binding.respuestaDos.text = it }
                    "Desnuda".also { binding.respuestaTres.text = it }
                    "Pijama".also { binding.respuestaCuatro.text = it }
                }
                7 -> {
                    "¿Te gustaría que te aten?".also { binding.preguntaView.text = it }
                    "Con una soga".also { binding.respuestaUno.text = it }
                    "Con esposas".also { binding.respuestaDos.text = it }
                    "Con sogas y vendas en los ojos".also { binding.respuestaTres.text = it }
                    "No me gustaría".also { binding.respuestaCuatro.text = it }
                }
                8 -> {
                    "Si se vienen en tu boca, tú:".also { binding.preguntaView.text = it }
                    "Escupes".also { binding.respuestaUno.text = it }
                    "Tragas".also { binding.respuestaDos.text = it }
                    "Pides más".also { binding.respuestaTres.text = it }
                    "Te enojas".also { binding.respuestaCuatro.text = it }
                }

                9 -> {
                    "¿Qué es lo más atrevido que has hecho en un cine?".also { binding.preguntaView.text = it }
                    "Un beso".also { binding.respuestaUno.text = it }
                    "Dedos mojados".also { binding.respuestaDos.text = it }
                    "Sexo oral".also { binding.respuestaTres.text = it }
                    "Sexo en lo alto de la sala".also { binding.respuestaCuatro.text = it }
                }
                10 -> {
                    "¿A qué edad perdiste la virginidad?".also { binding.preguntaView.text = it }
                    "13-15".also { binding.respuestaUno.text = it }
                    "16-18".also { binding.respuestaDos.text = it }
                    "18-22".also { binding.respuestaTres.text = it }
                    "22+".also { binding.respuestaCuatro.text = it }
                }
                11 -> {
                    "¿Que es lo más sucio que has pensado hacer con tu pareja?".also { binding.preguntaView.text = it }
                    "Sexo anal".also { binding.respuestaUno.text = it }
                    "Un trío".also { binding.respuestaDos.text = it }
                    "Intercambio de parejas".also { binding.respuestaTres.text = it }
                    "Una orgía".also { binding.respuestaCuatro.text = it }
                }
                12 -> {
                    "¿Alguna vez te has masturbado en un baño público?".also { binding.preguntaView.text = it }
                    "En el trabajo".also { binding.respuestaUno.text = it }
                    "En la escuela".also { binding.respuestaDos.text = it }
                    "En una estación de servicio".also { binding.respuestaTres.text = it }
                    "Nunca".also { binding.respuestaCuatro.text = it }
                }
            }

        }
        fun pruebaCatorce(i:Int){
            when(i) {

                1 -> {
                    "¿Con qué frecuencia te masturbas?".also { binding.preguntaView.text = it }
                    "Todos los días".also { binding.respuestaUno.text = it }
                    "Cada dos o tres días".also { binding.respuestaDos.text = it }
                    "Una vez por semana".also { binding.respuestaTres.text = it }
                    "Una vez al mes".also { binding.respuestaCuatro.text = it }
                }
                2 -> {
                    "¿Qué actitud te gusta que tenga tu pareja en la cama?".also { binding.preguntaView.text = it }
                    "Dominante".also { binding.respuestaUno.text = it }
                    "Pasiva".also { binding.respuestaDos.text = it }
                    "Sumisa".also { binding.respuestaTres.text = it }
                    "Violenta".also { binding.respuestaCuatro.text = it }
                }
                3 -> {
                    "¿Cuál es el máximo de orgasmos que has tenido en la misma noche?".also { binding.preguntaView.text = it }
                    "Apenas uno".also { binding.respuestaUno.text = it }
                    "Entre dos y tres".also { binding.respuestaDos.text = it }
                    "Entre cuatro y seis".also { binding.respuestaTres.text = it }
                    "Más de siete".also { binding.respuestaCuatro.text = it }
                }
                4 -> {
                    "¿En cuánto llegas al orgasmo en promedio?".also { binding.preguntaView.text = it }
                    "Cuestión de segundos".also { binding.respuestaUno.text = it }
                    "Cinco minutos".also { binding.respuestaDos.text = it }
                    "Veinte minutos".also { binding.respuestaTres.text = it }
                    "Una hora".also { binding.respuestaCuatro.text = it }
                }
                5 -> {
                    "¿Donde te gustaría recibir un masaje?".also { binding.preguntaView.text = it }
                    "Cuello".also { binding.respuestaUno.text = it }
                    "Espalda".also { binding.respuestaDos.text = it }
                    "Cola".also { binding.respuestaTres.text = it }
                    "Pies".also { binding.respuestaCuatro.text = it }
                }
                6 -> {
                    "¿Qué opinas de los chupones?".also { binding.preguntaView.text = it }
                    "Es infantil".also { binding.respuestaUno.text = it }
                    "Marco mi territorio".also { binding.respuestaDos.text = it }
                    "Me dan verguenza".also { binding.respuestaTres.text = it }
                    "Los llevo con orgullo".also { binding.respuestaCuatro.text = it }
                }
                7 -> {
                    "¿Que es lo más salvaje que has hecho?".also { binding.preguntaView.text = it }
                    "Romper un condón".also { binding.respuestaUno.text = it }
                    "Romper una cama".also { binding.respuestaDos.text = it }
                    "Sangrar por hacerlo tan fuerte".also { binding.respuestaTres.text = it }
                    "Dar una nalgada muy dolorosa".also { binding.respuestaCuatro.text = it }
                }
                8 -> {
                    "¿Cuál es tu fetiche?".also { binding.preguntaView.text = it }
                    "Sexo con los pies".also { binding.respuestaUno.text = it }
                    "Pegarle a tu pareja ".also { binding.respuestaDos.text = it }
                    "Escupir durante el sexo oral".also { binding.respuestaTres.text = it }
                    "Otro bastante raro".also { binding.respuestaCuatro.text = it }
                }

                9 -> {
                    "¿Qué pasa cuando hay conocidos en el cuarto de al lado?".also { binding.preguntaView.text = it }
                    "Se folla más fuerte".also { binding.respuestaUno.text = it }
                    "Se grita sin pudor".also { binding.respuestaDos.text = it }
                    "Se trata de guardar el mayor silencio posible".also { binding.respuestaTres.text = it }
                    "No se hace nada por respeto".also { binding.respuestaCuatro.text = it }
                }
                10 -> {
                    "¿Qué parte del cuerpo de tu pareja te gusta chupar?".also { binding.preguntaView.text = it }
                    "El cuello".also { binding.respuestaUno.text = it }
                    "Los pies".also { binding.respuestaDos.text = it }
                    "Los pechos".also { binding.respuestaTres.text = it }
                    "Sus partes íntimas".also { binding.respuestaCuatro.text = it }
                }
                11 -> {
                    "¿Cómo crees que la pasarías en una orgía?".also { binding.preguntaView.text = it }
                    "¡Genial!".also { binding.respuestaUno.text = it }
                    "Me daría verguenza".also { binding.respuestaDos.text = it }
                    "Depende de cómo la pase mi pareja".also { binding.respuestaTres.text = it }
                    "Nunca iría a una orgía y menos con mi amor".also { binding.respuestaCuatro.text = it }
                }
                12 -> {
                    "¿Tuviste relaciones de una noche?".also { binding.preguntaView.text = it }
                    "Una vez".also { binding.respuestaUno.text = it }
                    "Más de tres veces".also { binding.respuestaDos.text = it }
                    "Más de diez veces".also { binding.respuestaTres.text = it }
                    "Nunca".also { binding.respuestaCuatro.text = it }
                }
            }

        }
        fun pruebaQuince(i:Int){
            when(i) {

                1 -> {
                    "¿En donde te gusta más tener sexo?".also { binding.preguntaView.text = it }
                    "En la cama".also { binding.respuestaUno.text = it }
                    "En el sillón".also { binding.respuestaDos.text = it }
                    "En el baño".also { binding.respuestaTres.text = it }
                    "Sobre la mesa o el lavarropas".also { binding.respuestaCuatro.text = it }
                }
                2 -> {
                    "¿Venderías nudes?¿A qué precio?".also { binding.preguntaView.text = it }
                    "Mil dólares".also { binding.respuestaUno.text = it }
                    "Diez mil dólares ".also { binding.respuestaDos.text = it }
                    "Cien mil dólares o más".also { binding.respuestaTres.text = it }
                    "No vendería".also { binding.respuestaCuatro.text = it }
                }
                3 -> {
                    "¿Por qué precio dejarías que un desconocido haga lo que quiera contigo?".also { binding.preguntaView.text = it }
                    "Diez mil dólares".also { binding.respuestaUno.text = it }
                    "Cien mil dólares".also { binding.respuestaDos.text = it }
                    "Un millón o más".also { binding.respuestaTres.text = it }
                    "Mi dignidad no tiene precio".also { binding.respuestaCuatro.text = it }
                }
                4 -> {
                    "¿Recuerdas la primera vez que te sentiste excitado/a?".also { binding.preguntaView.text = it }
                    "A los 12".also { binding.respuestaUno.text = it }
                    "A los 13".also { binding.respuestaDos.text = it }
                    "A los 14".also { binding.respuestaTres.text = it }
                    "A los 15".also { binding.respuestaCuatro.text = it }
                }
                5 -> {
                    "¿Donde te gustaría recibir un masaje?".also { binding.preguntaView.text = it }
                    "Cuello".also { binding.respuestaUno.text = it }
                    "Espalda".also { binding.respuestaDos.text = it }
                    "Cola".also { binding.respuestaTres.text = it }
                    "Pies".also { binding.respuestaCuatro.text = it }
                }
                6 -> {
                    "¿Qué te gustaría que te hagan mientras conduces?".also { binding.preguntaView.text = it }
                    "Una manualidad".also { binding.respuestaUno.text = it }
                    "Una mamada".also { binding.respuestaDos.text = it }
                    "Que te digan cosas excitantes".also { binding.respuestaTres.text = it }
                    "Nada, es peligroso".also { binding.respuestaCuatro.text = it }
                }
                7 -> {
                    "¿Que es lo más grosero que has dicho follando?".also { binding.preguntaView.text = it }
                    "Eres mi puta".also { binding.respuestaUno.text = it }
                    "Quiero ser tu perrita".also { binding.respuestaDos.text = it }
                    "Quiero tu leche".also { binding.respuestaTres.text = it }
                    "Más duro Arturo".also { binding.respuestaCuatro.text = it }
                }
                8 -> {
                    "¿Cómo sería tu noche en pareja ideal?".also { binding.preguntaView.text = it }
                    "Con velas y pétalos de rosas".also { binding.respuestaUno.text = it }
                    "Follando bien duro sin parar".also { binding.respuestaDos.text = it }
                    "Viendo películas y teniendo sexo de vez en cuando".also { binding.respuestaTres.text = it }
                    "Adoptando roles para toda la noche".also { binding.respuestaCuatro.text = it }
                }

                9 -> {
                    "¿Qué piensas de las relaciones abiertas?".also { binding.preguntaView.text = it }
                    "No es para mi".also { binding.respuestaUno.text = it }
                    "Me gustaría probar".also { binding.respuestaDos.text = it }
                    "Me da curiosidad".also { binding.respuestaTres.text = it }
                    "Me moriría de celos".also { binding.respuestaCuatro.text = it }
                }
                10 -> {
                    "¿Cuando fue la última vez que tuviste un sueño sexual intenso?".also { binding.preguntaView.text = it }
                    "Esta semana".also { binding.respuestaUno.text = it }
                    "El mes pasado".also { binding.respuestaDos.text = it }
                    "Este año".also { binding.respuestaTres.text = it }
                    "Ya ni lo recuerdo".also { binding.respuestaCuatro.text = it }
                }
                11 -> {
                    "¿Cuánto tiempo crees que podrías estar sin follar?".also { binding.preguntaView.text = it }
                    "Un día".also { binding.respuestaUno.text = it }
                    "Una semana".also { binding.respuestaDos.text = it }
                    "Un mes".also { binding.respuestaTres.text = it }
                    "Un año".also { binding.respuestaCuatro.text = it }
                }
                12 -> {
                    "¿Cuál es el mejor momento del día para hacer el amor?".also { binding.preguntaView.text = it }
                    "A la mañana".also { binding.respuestaUno.text = it }
                    "A la tarde".also { binding.respuestaDos.text = it }
                    "A la noche".also { binding.respuestaTres.text = it }
                    "A la madrugada".also { binding.respuestaCuatro.text = it }
                }
            }

        }
        fun pruebaDiesiseis(i:Int){
            when(i) {

                1 -> {
                    "¿Qué agujero te da más placer?".also { binding.preguntaView.text = it }
                    "La boca".also { binding.respuestaUno.text = it }
                    "La vagina".also { binding.respuestaDos.text = it }
                    "El ano".also { binding.respuestaTres.text = it }
                    "Las manos o pies al masturbar".also { binding.respuestaCuatro.text = it }
                }
                2 -> {
                    "¿Antes de estar en pareja tuviste sexo prohibido?¿Con quién?".also { binding.preguntaView.text = it }
                    "Un amigo o amiga".also { binding.respuestaUno.text = it }
                    "Un o una familiar".also { binding.respuestaDos.text = it }
                    "Un/a compañero/a de trabajo".also { binding.respuestaTres.text = it }
                    "Nunca".also { binding.respuestaCuatro.text = it }
                }
                3 -> {
                    "¿Con cuál de estos tienes alguna fantasía?".also { binding.preguntaView.text = it }
                    "Un o una policía".also { binding.respuestaUno.text = it }
                    "Un sacerdote o monja".also { binding.respuestaDos.text = it }
                    "Un/a médico/a".also { binding.respuestaTres.text = it }
                    "Otros".also { binding.respuestaCuatro.text = it }
                }
                4 -> {
                    "¿Quién es tu permitido sexual?".also { binding.preguntaView.text = it }
                    "Un músico o música".also { binding.respuestaUno.text = it }
                    "Actor o actriz de cine".also { binding.respuestaDos.text = it }
                    "Un/a político/a que admiras ".also { binding.respuestaTres.text = it }
                    "Un/a atleta de élite".also { binding.respuestaCuatro.text = it }
                }
                5 -> {
                    "¿Alguna vez comiste mientras follabas?".also { binding.preguntaView.text = it }
                    "Fresas".also { binding.respuestaUno.text = it }
                    "Chocolate".also { binding.respuestaDos.text = it }
                    "Sushi".also { binding.respuestaTres.text = it }
                    "Nunca lo hice, lo hacemos la próxima vez".also { binding.respuestaCuatro.text = it }
                }
                6 -> {
                    "¿Cuántas veces tienes que follar con una persona para contarle lo que te gusta?".also { binding.preguntaView.text = it }
                    "A la primera".also { binding.respuestaUno.text = it }
                    "Dos o tres veces".also { binding.respuestaDos.text = it }
                    "Cuatro a siete veces".also { binding.respuestaTres.text = it }
                    "Necesito una relación larga para entrar en confianza".also { binding.respuestaCuatro.text = it }
                }
                7 -> {
                    "Cual es la mayor cantidad de veces seguidas que lo has hecho".also { binding.preguntaView.text = it }
                    "Dos".also { binding.respuestaUno.text = it }
                    "Tres".also { binding.respuestaDos.text = it }
                    "Cuatro".also { binding.respuestaTres.text = it }
                    "Cinco o más".also { binding.respuestaCuatro.text = it }
                }
                8 -> {
                    "¿Luces prendidas o apagadas?".also { binding.preguntaView.text = it }
                    "Prendidas, que se vea TODO".also { binding.respuestaUno.text = it }
                    "Apagadas, me da verguenza".also { binding.respuestaDos.text = it }
                    "Tenues, como la luz de las velas".also { binding.respuestaTres.text = it }
                    "Luces de colores cómo si fuera un show".also { binding.respuestaCuatro.text = it }
                }

                9 -> {
                    "¿Qué prefieres entre estas opciones?".also { binding.preguntaView.text = it }
                    "Lamer".also { binding.respuestaUno.text = it }
                    "Morder".also { binding.respuestaDos.text = it }
                    "Masajear".also { binding.respuestaTres.text = it }
                    "Escupir".also { binding.respuestaCuatro.text = it }
                }
                10 -> {
                    "¿Has enviado fotos de alguna de estas partes de tu cuerpo?".also { binding.preguntaView.text = it }
                    "Cola".also { binding.respuestaUno.text = it }
                    "Pechos".also { binding.respuestaDos.text = it }
                    "Partes íntimas".also { binding.respuestaTres.text = it }
                    "Todas las anteriores".also { binding.respuestaCuatro.text = it }
                }
                11 -> {
                    "¿Te gusta la lectura erótica?".also { binding.preguntaView.text = it }
                    "Sólo un mensaje de vez en cuando".also { binding.respuestaUno.text = it }
                    "Me gusta que mi pareja me excite con mensajes siempre que no la puedo ver".also { binding.respuestaDos.text = it }
                    "Leo novelas enteras".also { binding.respuestaTres.text = it }
                    "No me gusta".also { binding.respuestaCuatro.text = it }
                }
                12 -> {
                    "¿Qué tipo de porno te gusta más?".also { binding.preguntaView.text = it }
                    "Amateur".also { binding.respuestaUno.text = it }
                    "Interracial".also { binding.respuestaDos.text = it }
                    "Orgías".also { binding.respuestaTres.text = it }
                    "Sadomasoquismo".also { binding.respuestaCuatro.text = it }
                }
            }

        }

        fun initEvent(prueba:Int, i:Int){

            binding.respuestaUno.background.setTint(ContextCompat.getColor(this, R.color.rosa))
            binding.respuestaDos.background.setTint(ContextCompat.getColor(this, R.color.rosa))
            binding.respuestaTres.background.setTint(ContextCompat.getColor(this, R.color.rosa))
            binding.respuestaCuatro.background.setTint(ContextCompat.getColor(this, R.color.rosa))


            if (prueba ==1){
                pruebaUno(i)
            }else if (prueba ==2){
                pruebaDos(i)
            }else if (prueba ==3){
                pruebaTres(i)
            }else if (prueba ==4){
                pruebaCuatro(i)
            }else if (prueba ==5){
                pruebaCinco(i)
            }else if (prueba ==6){
                pruebaSeis(i)
            }else if (prueba ==7){
                pruebaSiete(i)
            }else if (prueba ==8){
                pruebaOcho(i)
            }else if (prueba ==9){
                pruebaNueve(i)
            }else if (prueba ==10){
                pruebaDiez(i)
            }else if (prueba ==11){
                pruebaOnce(i)
            }else if (prueba ==12){
                pruebaDoce(i)
            }else if (prueba ==13){
                pruebaTrece(i)
            }else if (prueba ==14){
                pruebaCatorce(i)
            }else if (prueba ==15){
                pruebaQuince(i)
            }else if (prueba ==16){
                pruebaDiesiseis(i)
            }
        }

        fun nosVamos(){
            promedio=comparaResp.toDouble()/12*100
            val intent = Intent(this, Respuestas::class.java)
            intent.putExtra("jugador1", jug1) //envio de datos a activities
            intent.putExtra("jugador2", jug2)
            intent.putExtra("prueba", prueba)
            intent.putExtra("promedio", promedio)
            startActivity(intent)//nos vamos
        }

        fun respuestaEvent(respuesta:Int){

                respuestasDos.add(respuesta)
                i++
                when(respuestas!![i-2]){
                    1-> binding.respuestaUno.background.setTint(ContextCompat.getColor(this, R.color.verde))
                    2-> binding.respuestaDos.background.setTint(ContextCompat.getColor(this, R.color.verde))
                    3-> binding.respuestaTres.background.setTint(ContextCompat.getColor(this, R.color.verde))
                    4-> binding.respuestaCuatro.background.setTint(ContextCompat.getColor(this, R.color.verde))
                }



                if (respuestas!![i-2] == respuestasDos!![i-2]){
                    comparaResp++

                }else {
                    when(respuestasDos!![i-2]){
                        1-> binding.respuestaUno.background.setTint(ContextCompat.getColor(this, R.color.rojo))
                        2-> binding.respuestaDos.background.setTint(ContextCompat.getColor(this, R.color.rojo))
                        3-> binding.respuestaTres.background.setTint(ContextCompat.getColor(this, R.color.rojo))
                        4-> binding.respuestaCuatro.background.setTint(ContextCompat.getColor(this, R.color.rojo))
                    }
                }

                if (i==13){
                    showAds()
                    interstitial?.fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            nosVamos()
                        }
                    }
                }else {
                    Timer("SettingUp", false).schedule(750) {
                        initEvent(prueba, i)
                        binding.contView.text = "Pregunta $i/12"
                    }
                }

        }


        binding.respuestaUno.setOnClickListener {respuestaEvent(1)}
        binding.respuestaDos.setOnClickListener {respuestaEvent(2) }
        binding.respuestaTres.setOnClickListener {respuestaEvent(3) }
        binding.respuestaCuatro.setOnClickListener {respuestaEvent(4) }


        initEvent(prueba, i)


    }

    private fun initLoadAds(){
        val adRequest: AdRequest =AdRequest.Builder().build()
        binding.banner.loadAd(adRequest)
    }
    private fun initIntAds() {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this, "ca-app-pub-4930505659937183/6385055420",
            adRequest, object : InterstitialAdLoadCallback(){
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitial = interstitialAd
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    interstitial = null
                }
            })
    }

    private fun showAds(){
        interstitial?.show(this)

    }
}