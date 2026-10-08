#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Genera app/src/main/assets/packs.json para la versión 4.0.

- Packs 1-10 (etapas de la relación): migrados de legacy-3x/scripts/generate_question_strings.py
  (legacy-3x/, versión 3.x), con correcciones de ortografía y formato.
- Packs 20-21 (amigos) y 30-32 (picante): contenido nuevo de la 4.0.
- Los packs adultos 11-16 de la 3.x NO se migran (texto explícito y dos preguntas
  con edades de menores, incompatibles con la política de Google Play).

Uso (desde la raíz del repo):  python3 tools/build_packs.py
A partir de la 4.0, packs.json es la fuente de verdad: para sumar packs se puede
editar el JSON directamente. Los "id" de pregunta no se deben reutilizar ni
renumerar, porque los links de desafío guardan esos ids.
"""
import json
import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
OLD_SCRIPT = os.path.join(ROOT, "legacy-3x", "scripts", "generate_question_strings.py")
OUT = os.path.join(ROOT, "app", "src", "main", "assets", "packs.json")

LANGS = ("es", "en", "pt")


def load_old_packs():
    src = open(OLD_SCRIPT, encoding="utf-8").read()
    start = src.index("PACKS = {")
    end = src.index("\n}\n", start) + 3
    ns = {}
    exec(src[start:end], ns)
    return ns["PACKS"]


ES_FIXES = {
    "extraordinria": "extraordinaria",
    "cancja de tennis": "cancha de tenis",
    "Qúe": "Qué",
    "vaciones": "vacaciones",
    "atividad": "actividad",
    "compania": "compañía",
    "Verguenza": "Vergüenza",
    "jóven": "joven",
    "Manejar un moto": "Manejar una moto",
    "Bateria": "Batería",
    "loteria": "lotería",
    "cómo regalo": "como regalo",
    "cómo tema": "como tema",
    "cómo mascota": "como mascota",
    "Dondes ": "Dónde ",
    "Tennis": "Tenis",
    "Basket": "Básquet",
    "Ciencia Ficción": "Ciencia ficción",
    "De Acción": "De acción",
    "El Leal": "El leal",
    "El lider": "El líder",
    "San Valentin": "San Valentín",
    "Algo Tonto": "Algo tonto",
    "Reality Shows": "Reality shows",
    "La estatua de la libertad": "La Estatua de la Libertad",
    "La gran muralla china": "La Gran Muralla China",
    "La torre Eiffel": "La Torre Eiffel",
    "Las pirámides de Egipto": "Las Pirámides de Egipto",
    "viendo series o películas": "Viendo series o películas",
    "Pilotear": "Pilotar",
    "¿de que tipo": "¿de qué tipo",
    "¿Cual sería?": "¿cuál sería?",
    "¿A donde irías": "¿Adónde irías",
    "Si sólo quedarán": "Si solo quedaran",
    "¿Qué harías?": "¿qué harías?",
    "¿Qué tipo de película sería?": "¿qué tipo de película sería?",
    "Describe tu estilo en una palabra": "Describe tu estilo en una palabra:",
    "Elige una de las siguientes mascotas": "Elige una de estas mascotas:",
}

INTERROGATIVES = ("Qué", "Que", "Cuál", "Cual", "Cuándo", "Cuando", "Dónde", "Donde",
                  "Cómo", "Como", "A qué", "A que", "En qué", "En que", "Cuántos", "A quién", "Por qué")
ACCENTS = [("¿Cual ", "¿Cuál "), ("¿Que ", "¿Qué "), ("¿Cuando ", "¿Cuándo "),
           ("¿Donde ", "¿Dónde "), ("¿A que ", "¿A qué "), ("¿En que ", "¿En qué "),
           ("¿Como ", "¿Cómo ")]


def fix_es_question(s):
    s = s.strip()
    for a, b in ES_FIXES.items():
        s = s.replace(a, b)
    if "¿" not in s and "?" not in s and s.startswith(INTERROGATIVES):
        s = "¿" + s + "?"
    elif s.endswith("?") and "¿" not in s:
        s = "¿" + s
    elif s.startswith("¿") and "?" not in s:
        s = s + "?"
    for a, b in ACCENTS:
        if s.startswith(a):
            s = b + s[len(a):]
    s = s.replace("¿Cuál es tu atividad", "¿Cuál es tu actividad")
    return s


def fix_answer(s, lang):
    s = s.strip()
    if lang == "es":
        for a, b in ES_FIXES.items():
            s = s.replace(a, b)
    letters = [c for c in s if c.isalpha()]
    # MAYÚSCULAS -> Oración, salvo siglas cortas (EEUU, USA, EUA)
    if len(letters) > 4 and all(c.isupper() for c in letters):
        s = s[0] + s[1:].lower()
    return s


def fix_q(s, lang):
    s = s.strip()
    if lang == "es":
        return fix_es_question(s)
    return s[0].upper() + s[1:] if s else s


COUPLE_META = {
    1: ("💘", ("Me gustas", "I Like You", "Gosto de você"),
        ("Para conocerse desde cero", "Getting to know each other", "Para se conhecer do zero")),
    2: ("☕", ("Primera cita", "First Date", "Primeiro encontro"),
        ("Gustos y costumbres", "Tastes and habits", "Gostos e costumes")),
    3: ("😍", ("Enamorados", "In Love", "Apaixonados"),
        ("Sueños y personalidad", "Dreams and personality", "Sonhos e personalidade")),
    4: ("💌", ("Seamos novios", "Be Mine", "Vamos namorar"),
        ("Qué los mueve", "What drives you", "O que move vocês")),
    5: ("💑", ("De novios", "Dating", "Namorando"),
        ("El día a día", "Everyday life", "O dia a dia")),
    6: ("💍", ("Cásate conmigo", "Marry Me", "Casa comigo"),
        ("Planes y fantasías", "Plans and daydreams", "Planos e fantasias")),
    7: ("💒", ("La boda", "The Wedding", "O casamento"),
        ("Gustos de pareja", "Couple tastes", "Gostos de casal")),
    8: ("🥈", ("Bodas de plata", "Silver Anniversary", "Bodas de prata"),
        ("Prioridades", "Priorities", "Prioridades")),
    9: ("🥇", ("Bodas de oro", "Golden Anniversary", "Bodas de ouro"),
        ("Hábitos y manías", "Habits and quirks", "Hábitos e manias")),
    10: ("💎", ("Bodas de platino", "Platinum Anniversary", "Bodas de platina"),
         ("Lo que de verdad importa", "What really matters", "O que realmente importa")),
}

# Pack 7 repite "¿Cuál es tu comida favorita?" con opciones de comidas del día.
DUP_FIX = {
    ("¿Cuál es tu comida favorita?", "Desayuno"): (
        "¿Cuál es tu comida preferida del día?",
        "Which meal of the day is your favorite?",
        "Qual é sua refeição favorita do dia?"),
}


def L(es, en, pt):
    return {"es": es, "en": en, "pt": pt}


def Q(q, a1, a2, a3, a4):
    """q y cada respuesta son tuplas (es, en, pt)."""
    return {"q": L(*q), "a": [L(*a1), L(*a2), L(*a3), L(*a4)]}


NEW_PACKS = [
    {
        "id": 20, "category": "friends", "premium": False, "emoji": "🤝",
        "name": L("Mejores amigos", "Best Friends", "Melhores amigos"),
        "subtitle": L("El test para tu mejor amigo o amiga", "The test for your best friend", "O teste para seu melhor amigo"),
        "questions": [
            Q(("¿Cuál es tu plan perfecto para un sábado?", "What's your perfect Saturday plan?", "Qual é seu plano perfeito para um sábado?"),
              ("Salir de fiesta", "Going out partying", "Sair para a balada"),
              ("Maratón de series", "A series marathon", "Maratona de séries"),
              ("Salir a comer", "Eating out", "Sair para comer"),
              ("Deporte o aire libre", "Sports or outdoors", "Esporte ou ar livre")),
            Q(("¿Qué comida pedirías si invito yo?", "What food would you order if I'm paying?", "Que comida você pediria se eu pagasse?"),
              ("Pizza", "Pizza", "Pizza"),
              ("Sushi", "Sushi", "Sushi"),
              ("Hamburguesa", "Burger", "Hambúrguer"),
              ("Comida mexicana", "Mexican food", "Comida mexicana")),
            Q(("¿Qué superpoder elegirías?", "Which superpower would you choose?", "Que superpoder você escolheria?"),
              ("Volar", "Flying", "Voar"),
              ("Ser invisible", "Invisibility", "Ficar invisível"),
              ("Leer mentes", "Reading minds", "Ler mentes"),
              ("Teletransportarme", "Teleporting", "Me teletransportar")),
            Q(("¿Cómo reaccionas ante una película de terror?", "How do you react to a horror movie?", "Como você reage a um filme de terror?"),
              ("Me encanta", "I love it", "Eu amo"),
              ("Miro entre los dedos", "I watch through my fingers", "Assisto por entre os dedos"),
              ("Me duermo", "I fall asleep", "Eu durmo"),
              ("No la veo ni loco/a", "No way I'm watching it", "Nem pensar em assistir")),
            Q(("¿Qué app usas más?", "Which app do you use the most?", "Qual app você mais usa?"),
              ("Instagram", "Instagram", "Instagram"),
              ("TikTok", "TikTok", "TikTok"),
              ("WhatsApp", "WhatsApp", "WhatsApp"),
              ("YouTube", "YouTube", "YouTube")),
            Q(("Si ganas la lotería, ¿qué haces primero?", "If you win the lottery, what do you do first?", "Se você ganhar na loteria, o que faz primeiro?"),
              ("Viajar", "Travel", "Viajar"),
              ("Comprar una casa", "Buy a house", "Comprar uma casa"),
              ("Invertir", "Invest", "Investir"),
              ("Ayudar a mi familia", "Help my family", "Ajudar minha família")),
            Q(("¿Cuál es tu mayor miedo?", "What's your biggest fear?", "Qual é seu maior medo?"),
              ("Las alturas", "Heights", "Altura"),
              ("Los insectos", "Bugs", "Insetos"),
              ("La soledad", "Being alone", "A solidão"),
              ("Hablar en público", "Public speaking", "Falar em público")),
            Q(("¿Qué tipo de amigo/a eres en una fiesta?", "What kind of friend are you at a party?", "Que tipo de amigo você é numa festa?"),
              ("El/la que baila toda la noche", "The one dancing all night", "O que dança a noite toda"),
              ("El/la que charla en la cocina", "The one chatting in the kitchen", "O que fica papeando na cozinha"),
              ("El/la que se va temprano", "The one who leaves early", "O que vai embora cedo"),
              ("El/la que elige la música", "The one picking the music", "O que escolhe a música")),
            Q(("¿Qué bebida no puede faltar en tu día?", "Which drink can't be missing from your day?", "Que bebida não pode faltar no seu dia?"),
              ("Café", "Coffee", "Café"),
              ("Mate o té", "Mate or tea", "Chimarrão ou chá"),
              ("Jugo", "Juice", "Suco"),
              ("Refresco", "Soda", "Refrigerante")),
            Q(("Si pudieras, ¿a qué hora te levantarías?", "If you could, what time would you wake up?", "Se pudesse, a que horas você acordaria?"),
              ("A las 7", "At 7", "Às 7"),
              ("A las 9", "At 9", "Às 9"),
              ("A las 11", "At 11", "Às 11"),
              ("Después del mediodía", "After noon", "Depois do meio-dia")),
            Q(("En un viaje sin plan, tú...", "On a trip with no plan, you...", "Numa viagem sem plano, você..."),
              ("Improvisas todo", "Improvise everything", "Improvisa tudo"),
              ("Buscas todo en internet", "Look everything up online", "Pesquisa tudo na internet"),
              ("Preguntas a la gente del lugar", "Ask the locals", "Pergunta para o pessoal do lugar"),
              ("Te quedas en el hotel", "Stay at the hotel", "Fica no hotel")),
            Q(("¿Qué es lo que más te molesta?", "What annoys you the most?", "O que mais te irrita?"),
              ("La impuntualidad", "People being late", "Atrasos"),
              ("El ruido", "Noise", "Barulho"),
              ("Las mentiras", "Lies", "Mentiras"),
              ("Que me interrumpan", "Being interrupted", "Ser interrompido")),
            Q(("¿Cuál es tu talento oculto?", "What's your hidden talent?", "Qual é seu talento escondido?"),
              ("Cocinar", "Cooking", "Cozinhar"),
              ("Cantar", "Singing", "Cantar"),
              ("Dibujar", "Drawing", "Desenhar"),
              ("Imitar voces", "Doing voices", "Imitar vozes")),
            Q(("¿Qué género de series prefieres?", "Which kind of series do you prefer?", "Que gênero de série você prefere?"),
              ("Comedia", "Comedy", "Comédia"),
              ("Drama", "Drama", "Drama"),
              ("Policiales", "Crime", "Policial"),
              ("Ciencia ficción", "Sci-fi", "Ficção científica")),
            Q(("¿Cómo sería tu cumpleaños ideal?", "What would your ideal birthday look like?", "Como seria seu aniversário ideal?"),
              ("Una gran fiesta", "A big party", "Uma grande festa"),
              ("Una cena con pocos", "A small dinner", "Um jantar com poucos"),
              ("Un viaje", "A trip", "Uma viagem"),
              ("Que pase desapercibido", "Keeping it low-key", "Que passe despercebido")),
        ],
    },
    {
        "id": 21, "category": "friends", "premium": False, "emoji": "🙌",
        "name": L("¿Me conoces de verdad?", "Do You Really Know Me?", "Você me conhece de verdade?"),
        "subtitle": L("Para amigos de toda la vida", "For lifelong friends", "Para amigos de longa data"),
        "questions": [
            Q(("¿Qué haces cuando estás triste?", "What do you do when you're sad?", "O que você faz quando está triste?"),
              ("Hablo con alguien", "Talk to someone", "Converso com alguém"),
              ("Escucho música", "Listen to music", "Escuto música"),
              ("Duermo", "Sleep", "Durmo"),
              ("Como algo rico", "Eat something tasty", "Como algo gostoso")),
            Q(("¿Qué es lo que más valoras en una amistad?", "What do you value most in a friendship?", "O que você mais valoriza numa amizade?"),
              ("La lealtad", "Loyalty", "Lealdade"),
              ("El humor", "Humor", "Humor"),
              ("La sinceridad", "Honesty", "Sinceridade"),
              ("Que esté siempre", "Always being there", "Estar sempre presente")),
            Q(("¿Cómo eras en la escuela?", "What were you like at school?", "Como você era na escola?"),
              ("Estudioso/a", "Studious", "Estudioso"),
              ("El/la payaso/a de la clase", "The class clown", "O palhaço da turma"),
              ("Callado/a", "Quiet", "Quieto"),
              ("Rebelde", "Rebellious", "Rebelde")),
            Q(("¿Qué cambiarías de tu rutina?", "What would you change about your routine?", "O que você mudaria na sua rotina?"),
              ("Dormir más", "Sleep more", "Dormir mais"),
              ("Hacer ejercicio", "Work out", "Fazer exercício"),
              ("Usar menos el celular", "Use my phone less", "Usar menos o celular"),
              ("Tener más tiempo libre", "Have more free time", "Ter mais tempo livre")),
            Q(("¿Cómo pides perdón?", "How do you apologize?", "Como você pede desculpas?"),
              ("Con palabras", "With words", "Com palavras"),
              ("Con un regalo", "With a gift", "Com um presente"),
              ("Con un abrazo", "With a hug", "Com um abraço"),
              ("Me cuesta mucho", "I find it really hard", "Tenho muita dificuldade")),
            Q(("¿Qué harías con un día libre sorpresa?", "What would you do with a surprise day off?", "O que você faria com um dia de folga surpresa?"),
              ("Dormir", "Sleep", "Dormir"),
              ("Salir con amigos", "Hang out with friends", "Sair com amigos"),
              ("Ordenar la casa", "Tidy up the house", "Arrumar a casa"),
              ("Ir a la naturaleza", "Get out in nature", "Ir para a natureza")),
            Q(("¿Qué te hace reír sin parar?", "What makes you laugh nonstop?", "O que te faz rir sem parar?"),
              ("Videos de animales", "Animal videos", "Vídeos de animais"),
              ("Memes", "Memes", "Memes"),
              ("Chistes malos", "Bad jokes", "Piadas ruins"),
              ("Las caídas", "People falling over", "Tombos")),
            Q(("¿Qué profesión tendrías en otra vida?", "What job would you have in another life?", "Que profissão você teria em outra vida?"),
              ("Artista", "Artist", "Artista"),
              ("Médico/a", "Doctor", "Médico"),
              ("Chef", "Chef", "Chef"),
              ("Viajero/a", "Traveler", "Viajante")),
            Q(("¿Cómo manejas el enojo?", "How do you handle anger?", "Como você lida com a raiva?"),
              ("Exploto y se me pasa", "I blow up and get over it", "Explodo e passa"),
              ("Me callo", "I go quiet", "Fico calado"),
              ("Lo hablo", "I talk it out", "Converso sobre isso"),
              ("Salgo a caminar", "I go for a walk", "Saio para caminhar")),
            Q(("¿Qué no puede faltar en tu mochila?", "What's always in your bag?", "O que não pode faltar na sua mochila?"),
              ("Auriculares", "Headphones", "Fones de ouvido"),
              ("Cargador", "Charger", "Carregador"),
              ("Algo para comer", "Something to eat", "Algo para comer"),
              ("Perfume", "Perfume", "Perfume")),
            Q(("¿Qué te da más nostalgia?", "What makes you most nostalgic?", "O que te dá mais saudade?"),
              ("Mi infancia", "My childhood", "Minha infância"),
              ("Un viaje", "A trip", "Uma viagem"),
              ("Un amor del pasado", "A past love", "Um amor do passado"),
              ("Mis amigos de antes", "Old friends", "Meus amigos de antes")),
            Q(("¿Qué tipo de regalo te gusta más?", "What kind of gift do you like best?", "Que tipo de presente você mais gosta?"),
              ("Algo hecho a mano", "Something handmade", "Algo feito à mão"),
              ("Una experiencia", "An experience", "Uma experiência"),
              ("Algo útil", "Something useful", "Algo útil"),
              ("Una sorpresa", "A surprise", "Uma surpresa")),
            Q(("¿Cuál es tu mayor sueño?", "What's your biggest dream?", "Qual é seu maior sonho?"),
              ("Viajar por el mundo", "Travel the world", "Viajar pelo mundo"),
              ("Tener mi propio negocio", "Run my own business", "Ter meu próprio negócio"),
              ("Formar una familia", "Start a family", "Formar uma família"),
              ("Ser famoso/a", "Be famous", "Ser famoso")),
            Q(("¿Cómo te describirían tus amigos?", "How would your friends describe you?", "Como seus amigos te descreveriam?"),
              ("Divertido/a", "Fun", "Divertido"),
              ("Leal", "Loyal", "Leal"),
              ("Despistado/a", "Absent-minded", "Distraído"),
              ("Mandón/a", "Bossy", "Mandão")),
            Q(("Si te enteras de un chisme, tú...", "If you hear some gossip, you...", "Se você fica sabendo de uma fofoca, você..."),
              ("Lo cuentas enseguida", "Tell someone right away", "Conta na hora"),
              ("Lo guardas", "Keep it to yourself", "Guarda para si"),
              ("Primero lo confirmas", "Check it first", "Confirma primeiro"),
              ("Te haces el/la que no sabe", "Act like you don't know", "Finge que não sabe")),
        ],
    },
    {
        "id": 30, "category": "spicy", "premium": False, "emoji": "😏",
        "name": L("Coqueteo", "Flirting", "Paquera"),
        "subtitle": L("Para empezar a subir la temperatura", "To start turning up the heat", "Para começar a esquentar"),
        "questions": [
            Q(("¿Qué es lo primero que te atrae de alguien?", "What's the first thing that attracts you to someone?", "O que primeiro te atrai em alguém?"),
              ("La mirada", "Their eyes", "O olhar"),
              ("La sonrisa", "Their smile", "O sorriso"),
              ("La forma de hablar", "The way they talk", "O jeito de falar"),
              ("El perfume", "Their scent", "O perfume")),
            Q(("¿Cuál es tu forma favorita de coquetear?", "What's your favorite way to flirt?", "Qual é seu jeito favorito de paquerar?"),
              ("Con la mirada", "With a look", "Com o olhar"),
              ("Con humor", "With humor", "Com humor"),
              ("Con mensajes", "By text", "Por mensagem"),
              ("Directo al grano", "Straight to the point", "Direto ao ponto")),
            Q(("¿Qué mensaje te encantaría recibir a medianoche?", "Which message would you love to get at midnight?", "Que mensagem você adoraria receber à meia-noite?"),
              ("\"Te extraño\"", "\"I miss you\"", "\"Saudade de você\""),
              ("\"¿Estás despierto/a?\"", "\"You up?\"", "\"Tá acordado?\""),
              ("Una foto linda", "A cute photo", "Uma foto bonita"),
              ("Una canción dedicada", "A song dedicated to me", "Uma música dedicada")),
            Q(("¿Dónde prefieres un beso inesperado?", "Where do you prefer a surprise kiss?", "Onde você prefere um beijo inesperado?"),
              ("En el cuello", "On the neck", "No pescoço"),
              ("En la frente", "On the forehead", "Na testa"),
              ("En la mano", "On the hand", "Na mão"),
              ("En los labios", "On the lips", "Na boca")),
            Q(("¿Qué estilo de ropa te parece más atractivo en tu pareja?", "Which outfit style do you find most attractive on your partner?", "Que estilo de roupa você acha mais atraente no seu par?"),
              ("Camisa blanca", "A white shirt", "Camisa branca"),
              ("Ropa deportiva", "Sportswear", "Roupa esportiva"),
              ("Algo elegante", "Something elegant", "Algo elegante"),
              ("Su pijama", "Their pajamas", "O pijama")),
            Q(("¿Qué te pone más nervioso/a en una cita?", "What makes you most nervous on a date?", "O que mais te deixa nervoso num encontro?"),
              ("El primer beso", "The first kiss", "O primeiro beijo"),
              ("Los silencios", "Awkward silences", "Os silêncios"),
              ("Elegir qué ponerme", "Choosing what to wear", "Escolher o que vestir"),
              ("La cuenta", "The bill", "A conta")),
            Q(("¿Qué apodo cariñoso te encanta?", "Which pet name do you love?", "Qual apelido carinhoso você adora?"),
              ("Amor", "Love", "Amor"),
              ("Bebé", "Baby", "Bebê"),
              ("Cielo", "Sweetheart", "Meu bem"),
              ("Mi nombre, bien dicho", "Just my name, said right", "Meu nome, bem dito")),
            Q(("¿Cuál es tu plan ideal para una noche romántica?", "What's your ideal romantic night plan?", "Qual é seu plano ideal para uma noite romântica?"),
              ("Cena con velas", "Candlelit dinner", "Jantar à luz de velas"),
              ("Película abrazados", "Cuddling over a movie", "Filme agarradinhos"),
              ("Salir a bailar", "Going dancing", "Sair para dançar"),
              ("Una escapada sorpresa", "A surprise getaway", "Uma escapada surpresa")),
            Q(("¿Qué tan celoso/a eres?", "How jealous are you?", "O quanto você é ciumento?"),
              ("Nada", "Not at all", "Nada"),
              ("Un poquito", "A little", "Um pouquinho"),
              ("Bastante", "Quite a bit", "Bastante"),
              ("Mucho, pero lo disimulo", "Very, but I hide it", "Muito, mas disfarço")),
            Q(("¿Qué te parece más sexy?", "What do you find sexiest?", "O que você acha mais sexy?"),
              ("La inteligencia", "Intelligence", "Inteligência"),
              ("El sentido del humor", "A sense of humor", "Senso de humor"),
              ("La seguridad", "Confidence", "Confiança"),
              ("La ternura", "Tenderness", "Carinho")),
            Q(("Si te dedican una canción, prefieres que sea...", "If someone dedicates a song to you, you'd want it to be...", "Se dedicarem uma música para você, prefere que seja..."),
              ("Romántica", "Romantic", "Romântica"),
              ("Divertida", "Fun", "Divertida"),
              ("Sensual", "Sensual", "Sensual"),
              ("\"Nuestra canción\"", "\"Our song\"", "\"Nossa música\"")),
            Q(("¿Cuánto tardas en responder a alguien que te gusta?", "How long do you take to reply to someone you like?", "Quanto tempo você demora para responder alguém de quem gosta?"),
              ("Al instante", "Instantly", "Na hora"),
              ("Unos minutos, para disimular", "A few minutes, to play it cool", "Alguns minutos, para disfarçar"),
              ("Horas", "Hours", "Horas"),
              ("Depende del mensaje", "Depends on the message", "Depende da mensagem")),
            Q(("¿Qué parte de una cita disfrutas más?", "Which part of a date do you enjoy most?", "Que parte de um encontro você curte mais?"),
              ("La previa", "The build-up", "A expectativa"),
              ("La charla", "The conversation", "A conversa"),
              ("El final de la noche", "The end of the night", "O fim da noite"),
              ("El mensaje del día siguiente", "The text the next day", "A mensagem no dia seguinte")),
            Q(("¿Qué te enamora más?", "What makes you fall in love the most?", "O que mais te apaixona?"),
              ("Los detalles", "Little gestures", "Os detalhes"),
              ("Las palabras", "Words", "As palavras"),
              ("Los abrazos", "Hugs", "Os abraços"),
              ("El tiempo juntos", "Time together", "O tempo juntos")),
            Q(("¿Quién debería dar el primer paso?", "Who should make the first move?", "Quem deveria dar o primeiro passo?"),
              ("Yo", "Me", "Eu"),
              ("La otra persona", "The other person", "A outra pessoa"),
              ("El que se anime", "Whoever dares", "Quem tiver coragem"),
              ("Nos miramos y listo", "We just look at each other and go", "A gente se olha e pronto")),
        ],
    },
    {
        "id": 31, "category": "spicy", "premium": True, "emoji": "🔥",
        "name": L("Química", "Chemistry", "Química"),
        "subtitle": L("Para parejas con confianza", "For couples who trust each other", "Para casais com intimidade"),
        "questions": [
            Q(("¿En qué momento del día te sientes más romántico/a?", "When during the day do you feel most romantic?", "Em que momento do dia você se sente mais romântico?"),
              ("Al despertar", "When I wake up", "Ao acordar"),
              ("A la tarde", "In the afternoon", "À tarde"),
              ("A la noche", "At night", "À noite"),
              ("De madrugada", "Late at night", "De madrugada")),
            Q(("¿Qué tipo de besos prefieres?", "What kind of kisses do you prefer?", "Que tipo de beijo você prefere?"),
              ("Tiernos", "Sweet", "Carinhosos"),
              ("Apasionados", "Passionate", "Apaixonados"),
              ("Juguetones", "Playful", "Brincalhões"),
              ("Largos y lentos", "Long and slow", "Longos e lentos")),
            Q(("¿Luces prendidas o apagadas?", "Lights on or off?", "Luz acesa ou apagada?"),
              ("Prendidas", "On", "Acesa"),
              ("Apagadas", "Off", "Apagada"),
              ("Velas", "Candles", "Velas"),
              ("Luces de colores", "Colored lights", "Luzes coloridas")),
            Q(("¿Qué perfume en tu pareja te vuelve loco/a?", "Which scent on your partner drives you crazy?", "Que perfume no seu par te deixa louco?"),
              ("Dulce", "Sweet", "Doce"),
              ("Cítrico", "Citrus", "Cítrico"),
              ("Amaderado", "Woody", "Amadeirado"),
              ("Su olor natural", "Their natural scent", "O cheiro natural")),
            Q(("¿Te animarías a un juego de roles?", "Would you try role-play?", "Você toparia um jogo de papéis?"),
              ("Me encantaría", "I'd love to", "Adoraria"),
              ("Me da curiosidad", "I'm curious", "Tenho curiosidade"),
              ("Solo si lo propone mi pareja", "Only if my partner suggests it", "Só se meu par propuser"),
              ("Prefiero que no", "I'd rather not", "Prefiro não")),
            Q(("¿Qué plan te parece más atrevido?", "Which plan sounds most daring to you?", "Que plano parece mais ousado para você?"),
              ("Mensajes subidos de tono", "Steamy texting", "Mensagens picantes"),
              ("Un baile privado", "A private dance", "Uma dança particular"),
              ("Un baño de espuma juntos", "A bubble bath together", "Um banho de espuma juntos"),
              ("Un masaje con aceites", "An oil massage", "Uma massagem com óleo")),
            Q(("¿Dónde te gustaría recibir un masaje?", "Where would you like a massage?", "Onde você gostaria de receber uma massagem?"),
              ("En el cuello", "Neck", "No pescoço"),
              ("En la espalda", "Back", "Nas costas"),
              ("En los pies", "Feet", "Nos pés"),
              ("En las manos", "Hands", "Nas mãos")),
            Q(("¿Cuál es tu fantasía de escapada?", "What's your dream getaway?", "Qual é sua fantasia de escapada?"),
              ("Una cabaña en la montaña", "A mountain cabin", "Um chalé na montanha"),
              ("Un hotel de lujo", "A luxury hotel", "Um hotel de luxo"),
              ("Una playa desierta", "A deserted beach", "Uma praia deserta"),
              ("Una ciudad desconocida", "A city we don't know", "Uma cidade desconhecida")),
            Q(("¿Qué piensas de las relaciones abiertas?", "What do you think about open relationships?", "O que você acha de relacionamentos abertos?"),
              ("No son para mí", "Not for me", "Não são para mim"),
              ("Me dan curiosidad", "I'm curious", "Tenho curiosidade"),
              ("Me gustaría probar", "I'd like to try", "Gostaria de experimentar"),
              ("Me moriría de celos", "I'd die of jealousy", "Morreria de ciúmes")),
            Q(("¿Qué es lo más romántico que hiciste por alguien?", "What's the most romantic thing you've done for someone?", "Qual foi a coisa mais romântica que você fez por alguém?"),
              ("Una sorpresa", "A surprise", "Uma surpresa"),
              ("Una carta", "A letter", "Uma carta"),
              ("Un viaje", "A trip", "Uma viagem"),
              ("Una serenata", "A serenade", "Uma serenata")),
            Q(("¿Cuál es tu momento favorito para estar a solas con tu pareja?", "What's your favorite time to be alone with your partner?", "Qual é seu momento favorito para ficar a sós com seu par?"),
              ("A la mañana", "Morning", "De manhã"),
              ("A la tarde", "Afternoon", "À tarde"),
              ("A la noche", "Night", "À noite"),
              ("De madrugada", "The small hours", "De madrugada")),
            Q(("Si tu pareja te manda una foto atrevida, tú...", "If your partner sends you a daring photo, you...", "Se seu par te manda uma foto ousada, você..."),
              ("Respondes con otra", "Send one back", "Responde com outra"),
              ("Te pones colorado/a", "Blush", "Fica vermelho"),
              ("La guardas bajo llave", "Keep it under lock and key", "Guarda a sete chaves"),
              ("Llamas al instante", "Call right away", "Liga na hora")),
            Q(("¿Qué música elegirías para una noche especial?", "What music would you pick for a special night?", "Que música você escolheria para uma noite especial?"),
              ("R&B", "R&B", "R&B"),
              ("Baladas", "Ballads", "Baladas românticas"),
              ("Reggaetón", "Reggaeton", "Reggaeton"),
              ("Jazz", "Jazz", "Jazz")),
            Q(("¿Qué tan aventurero/a eres en la intimidad?", "How adventurous are you in private?", "O quanto você é aventureiro na intimidade?"),
              ("Muy clásico/a", "Very classic", "Bem clássico"),
              ("Me gusta probar cosas", "I like trying things", "Gosto de experimentar"),
              ("Bastante aventurero/a", "Quite adventurous", "Bem aventureiro"),
              ("Depende del día", "Depends on the day", "Depende do dia")),
            Q(("¿Qué gesto te derrite?", "Which gesture melts you?", "Que gesto te derrete?"),
              ("Un abrazo por la espalda", "A hug from behind", "Um abraço por trás"),
              ("Que me acaricien el pelo", "Someone playing with my hair", "Cafuné"),
              ("Una mirada fija", "A long look", "Um olhar fixo"),
              ("Un susurro al oído", "A whisper in my ear", "Um sussurro no ouvido")),
        ],
    },
    {
        "id": 32, "category": "spicy", "premium": True, "emoji": "🌶️",
        "name": L("Atrevidos", "Daring", "Ousados"),
        "subtitle": L("Solo para valientes", "Only for the brave", "Só para corajosos"),
        "questions": [
            Q(("¿Cuál es el lugar más atrevido donde diste un beso apasionado?", "What's the boldest place you've had a passionate kiss?", "Qual o lugar mais ousado onde você deu um beijo apaixonado?"),
              ("En el cine", "At the movies", "No cinema"),
              ("En la playa", "At the beach", "Na praia"),
              ("En un ascensor", "In an elevator", "No elevador"),
              ("En una fiesta", "At a party", "Numa festa")),
            Q(("¿Qué tan lejos llegas en verdad o reto?", "How far do you go in truth or dare?", "Até onde você vai no verdade ou desafio?"),
              ("Solo verdades", "Truths only", "Só verdades"),
              ("Retos suaves", "Mild dares", "Desafios leves"),
              ("Cualquier reto", "Any dare", "Qualquer desafio"),
              ("Hasta donde dé", "As far as it goes", "Até onde der")),
            Q(("¿Qué disfraz te gustaría ver en tu pareja?", "Which costume would you like to see on your partner?", "Que fantasia você gostaria de ver no seu par?"),
              ("Superhéroe", "Superhero", "Super-herói"),
              ("Policía", "Police officer", "Policial"),
              ("Pirata", "Pirate", "Pirata"),
              ("Algo de gala", "Something black-tie", "Algo de gala")),
            Q(("¿Qué mensajes te gusta recibir cuando están lejos?", "What messages do you like getting when you're apart?", "Que mensagens você gosta de receber quando estão longe?"),
              ("Románticos", "Romantic", "Românticas"),
              ("Picantes", "Spicy", "Picantes"),
              ("Divertidos", "Funny", "Divertidas"),
              ("Audios larguísimos", "Super long voice notes", "Áudios enormes")),
            Q(("¿Alguna vez mandaste un mensaje atrevido a la persona equivocada?", "Have you ever sent a daring message to the wrong person?", "Você já mandou uma mensagem ousada para a pessoa errada?"),
              ("Sí, qué vergüenza", "Yes, so embarrassing", "Sim, que vergonha"),
              ("Casi", "Almost", "Quase"),
              ("Nunca", "Never", "Nunca"),
              ("Prefiero no responder", "I'd rather not say", "Prefiro não responder")),
            Q(("¿Qué te gustaría que tu pareja te susurre al oído?", "What would you like your partner to whisper in your ear?", "O que você gostaria que seu par sussurrasse no seu ouvido?"),
              ("\"Te amo\"", "\"I love you\"", "\"Eu te amo\""),
              ("Un secreto", "A secret", "Um segredo"),
              ("El plan de esta noche", "Tonight's plan", "O plano desta noite"),
              ("Un piropo atrevido", "A cheeky compliment", "Um elogio ousado")),
            Q(("¿Cuánto aguantas sin un beso de tu pareja?", "How long can you go without a kiss from your partner?", "Quanto tempo você aguenta sem um beijo do seu par?"),
              ("Un día", "A day", "Um dia"),
              ("Una semana", "A week", "Uma semana"),
              ("Un mes", "A month", "Um mês"),
              ("Ni una hora", "Not even an hour", "Nem uma hora")),
            Q(("Para una noche juntos, prefieres algo...", "For a night together, you prefer something...", "Para uma noite juntos, você prefere algo..."),
              ("Tranquilo", "Calm", "Tranquilo"),
              ("Intenso", "Intense", "Intenso"),
              ("Espontáneo", "Spontaneous", "Espontâneo"),
              ("Bien planeado", "Well planned", "Bem planejado")),
            Q(("¿Te animarías a hacerle un baile sensual a tu pareja?", "Would you do a sensual dance for your partner?", "Você toparia fazer uma dança sensual para seu par?"),
              ("Ya lo hice", "Already have", "Já fiz"),
              ("Sí, con práctica", "Yes, with practice", "Sim, com treino"),
              ("Con las luces apagadas", "With the lights off", "Com a luz apagada"),
              ("Jamás", "Never", "Jamais")),
            Q(("¿Qué te da más vergüenza?", "What embarrasses you the most?", "O que te dá mais vergonha?"),
              ("Que me vean bailando", "Being seen dancing", "Me verem dançando"),
              ("Que lean mis mensajes", "Someone reading my messages", "Lerem minhas mensagens"),
              ("Que me escuchen cantar", "Being heard singing", "Me ouvirem cantar"),
              ("Que me descubran mirando", "Getting caught staring", "Me pegarem olhando")),
            Q(("¿Qué usas para dormir?", "What do you wear to bed?", "O que você usa para dormir?"),
              ("Pijama", "Pajamas", "Pijama"),
              ("Una camiseta grande", "A big T-shirt", "Uma camiseta grande"),
              ("Lo menos posible", "As little as possible", "O mínimo possível"),
              ("Ropa interior linda", "Nice underwear", "Uma lingerie bonita")),
            Q(("¿Qué harías con una noche de hotel gratis?", "What would you do with a free hotel night?", "O que você faria com uma noite de hotel grátis?"),
              ("Dormir como nunca", "Sleep like never before", "Dormir como nunca"),
              ("Cena y spa", "Dinner and spa", "Jantar e spa"),
              ("Una noche muy romántica", "A very romantic night", "Uma noite bem romântica"),
              ("Fiesta en la habitación", "Room party", "Festa no quarto")),
            Q(("¿Qué beso te desarma?", "Which kiss disarms you?", "Que beijo te desmonta?"),
              ("En el cuello", "On the neck", "No pescoço"),
              ("En la frente", "On the forehead", "Na testa"),
              ("Uno sorpresa por la espalda", "A surprise one from behind", "Um de surpresa por trás"),
              ("Uno largo bajo la lluvia", "A long one in the rain", "Um longo na chuva")),
            Q(("Si tu pareja te diera permiso, ¿con qué famoso tendrías una cita?", "If your partner allowed it, which celebrity would you date?", "Se seu par deixasse, com que famoso você sairia?"),
              ("Un actor o actriz", "An actor or actress", "Um ator ou atriz"),
              ("Un músico o cantante", "A musician or singer", "Um músico ou cantor"),
              ("Un deportista", "An athlete", "Um atleta"),
              ("Ninguno, solo tengo ojos para mi pareja", "Nobody, I only have eyes for my partner", "Ninguém, só tenho olhos para meu par")),
            Q(("¿Qué tan bien guardas los secretos de pareja?", "How well do you keep couple secrets?", "O quanto você guarda segredos do casal?"),
              ("Soy una tumba", "I'm a vault", "Sou um túmulo"),
              ("Se lo cuento a mi mejor amigo/a", "I tell my best friend", "Conto para meu melhor amigo"),
              ("Se me escapan", "They slip out", "Eles escapam"),
              ("Depende del secreto", "Depends on the secret", "Depende do segredo")),
        ],
    },
]


def build():
    old = load_old_packs()
    packs = []
    for pid in range(1, 11):
        emoji, names, subs = COUPLE_META[pid]
        questions = []
        for i, item in enumerate(old[pid], start=1):
            q = {"es": fix_q(item[0], "es"), "en": fix_q(item[1], "en"), "pt": fix_q(item[2], "pt")}
            answers = [{lang: fix_answer(a[k], lang) for k, lang in enumerate(LANGS)} for a in item[3:]]
            key = (q["es"], answers[0]["es"])
            if key in DUP_FIX:
                q = L(*DUP_FIX[key])
            questions.append({"id": i, "q": q, "a": answers})
        packs.append({
            "id": pid, "category": "couple", "premium": False, "emoji": emoji,
            "name": L(*names), "subtitle": L(*subs), "questions": questions,
        })
    for p in NEW_PACKS:
        p = dict(p)
        p["questions"] = [dict(q, id=i) for i, q in enumerate(p["questions"], start=1)]
        packs.append(p)

    # Validaciones
    ids = [p["id"] for p in packs]
    assert len(ids) == len(set(ids)), "ids de pack repetidos"
    for p in packs:
        assert len(p["questions"]) >= 10, (p["id"], "menos de 10 preguntas")
        qids = [q["id"] for q in p["questions"]]
        assert len(qids) == len(set(qids)) and max(qids) < 256, p["id"]
        for q in p["questions"]:
            assert len(q["a"]) == 4, (p["id"], q["id"])
            for lang in LANGS:
                assert q["q"][lang].strip(), (p["id"], q["id"], lang)
                for a in q["a"]:
                    assert a[lang].strip(), (p["id"], q["id"], lang)

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump({"version": 1, "packs": packs}, f, ensure_ascii=False, indent=1)
    total = sum(len(p["questions"]) for p in packs)
    print(f"OK: {len(packs)} packs, {total} preguntas -> {OUT}")


if __name__ == "__main__":
    build()
