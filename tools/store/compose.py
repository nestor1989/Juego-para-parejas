import asyncio, sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parent))
import screens as SC
import icon
from playwright.async_api import async_playwright
from PIL import Image

OUT = Path(__file__).resolve().parent / 'out'

CAPTIONS = {
 'es': [('result', '¿Cuánto me conoces?', 'Descubran su % de compatibilidad'),
        ('remote', 'Juega a distancia', 'Envía tu desafío por WhatsApp'),
        ('review', 'Mira en qué coincidieron', 'Repaso pregunta por pregunta'),
        ('question', 'Uno responde, el otro adivina', 'Cada acierto suma'),
        ('packs', 'Packs para cada etapa', 'De la primera cita a las bodas de oro'),
        ('friends', 'También con amigos', 'Y packs picantes para adultos'),
        ('handoff', 'Sin internet, en un celular', 'Pásense el teléfono y jueguen'),
        ('premium', 'Premium para siempre', 'Un solo pago, sin suscripción')],
 'pt': [('result', 'O quanto você me conhece?', 'Descubram a % de compatibilidade'),
        ('remote', 'Jogue a distância', 'Envie seu desafio pelo WhatsApp'),
        ('review', 'Vejam em que acertaram', 'Revisão pergunta por pergunta'),
        ('question', 'Um responde, o outro adivinha', 'Cada acerto conta'),
        ('packs', 'Pacotes para cada fase', 'Do primeiro encontro às bodas de ouro'),
        ('friends', 'Também com amigos', 'E pacotes picantes para adultos'),
        ('handoff', 'Sem internet, num celular', 'Passem o celular e joguem'),
        ('premium', 'Premium para sempre', 'Pagamento único, sem assinatura')],
 'en': [('result', 'How well do you know me?', 'Find out your compatibility score'),
        ('remote', 'Play long distance', 'Send your challenge on WhatsApp'),
        ('review', 'See where you matched', 'Question-by-question review'),
        ('question', 'One answers, one guesses', 'Every match counts'),
        ('packs', 'Packs for every stage', 'From first date to golden anniversary'),
        ('friends', 'Play with friends too', 'Plus spicy packs for adults'),
        ('handoff', 'Offline, on one phone', 'Just pass the phone'),
        ('premium', 'Lifetime premium', 'One payment, no subscription')],
}

FEATURE = {
 'es': ('Juego para Parejas', '¿Cuánto me conoces?', 'Juntos o a distancia'),
 'pt': ('Jogo para Casais', 'O quanto você me conhece?', 'Juntos ou a distância'),
 'en': ('Couples Quiz', 'How well do you know me?', 'Together or long distance'),
}

BG = 'linear-gradient(160deg, #F2386F 0%, #B23BA0 55%, #7D41D3 100%)'

def deco_hearts():
    shapes, _ = icon.variant_heart()
    heart_d = shapes[0][1]
    spots = [(-40, 380, 220, .08, -14), (900, 300, 180, .09, 16), (860, 1500, 260, .07, -10), (-60, 1300, 200, .06, 12)]
    out = []
    for x, y, s, op, rot in spots:
        out.append(f'<svg style="position:absolute;left:{x}px;top:{y}px;transform:rotate({rot}deg)" width="{s}" height="{s}" viewBox="20 20 68 68">'
                   f'<path d="{heart_d}" fill="#fff" fill-opacity="{op}"/></svg>')
    return ''.join(out)

def page(inner, w, h):
    return (f'<html><head><style>{SC.font_faces()} body{{margin:0}} {SC.APP_CSS}</style></head>'
            f'<body><div style="width:{w}px;height:{h}px;position:relative;overflow:hidden">{inner}</div></body></html>')

def phone(screen_html, width):
    scale = width / 412
    h = 892 * scale
    bez = round(width * 0.031)
    return (f'<div style="width:{width + 2*bez}px;height:{h + 2*bez}px;background:#141014;border-radius:{width*0.112}px;'
            f'padding:{bez}px;box-shadow:0 40px 90px rgba(30,0,40,.38);box-sizing:border-box;position:relative">'
            f'<div style="width:{width}px;height:{h}px;border-radius:{width*0.085}px;overflow:hidden;position:relative">'
            f'<div style="transform:scale({scale});transform-origin:0 0;width:412px;height:892px">{screen_html}</div>'
            f'<div style="position:absolute;top:{10*scale}px;left:50%;width:{12*scale}px;height:{12*scale}px;margin-left:{-6*scale}px;'
            f'border-radius:50%;background:#141014"></div></div></div>')

def screenshot_html(lang, key, title, sub):
    S = SC.strings(lang)
    fn = SC.SCREENS[key]
    scr = fn(S, lang)
    inner = (f'<div style="position:absolute;inset:0;background:{BG}"></div>{deco_hearts()}'
             f'<div style="position:absolute;top:96px;left:60px;right:60px;text-align:center;color:#fff;font-family:Roboto">'
             f'<div style="font-weight:900;font-size:74px;line-height:1.12;letter-spacing:-0.5px">{SC.esc(title)}</div>'
             f'<div style="font-weight:500;font-size:40px;line-height:1.3;opacity:.93;margin-top:22px">{SC.esc(sub)}</div></div>'
             f'<div style="position:absolute;left:{(1080 - 760 - 2 * round(760 * 0.031)) // 2}px;top:420px">{phone(scr, 760)}</div>')
    return page(inner, 1080, 1920)

def app_icon_svg(size, rounded=True):
    shapes, _ = icon.variant_heart()
    svg = icon.svg(shapes, size, 'square')
    return svg.replace('viewBox="0 0 108 108"', 'viewBox="14 14 80 80"')

def feature_html(lang):
    title, sub, pill = FEATURE[lang]
    S = SC.strings(lang)
    icon_svg = app_icon_svg(132)
    p1 = phone(SC.SCREENS['result'](S, lang), 236)
    p2 = phone(SC.SCREENS['question'](S, lang), 236)
    inner = (f'<div style="position:absolute;inset:0;background:linear-gradient(120deg,#F2386F 0%,#B23BA0 55%,#7D41D3 100%)"></div>'
             f'<div style="position:absolute;left:64px;top:92px;width:470px;color:#fff;font-family:Roboto">'
             f'<div style="width:132px;height:132px;border-radius:30px;overflow:hidden;box-shadow:0 12px 30px rgba(30,0,40,.3)">{icon_svg}</div>'
             f'<div style="font-family:Aladin;font-size:62px;line-height:1.05;margin-top:26px">{SC.esc(title)}</div>'
             f'<div style="font-weight:500;font-size:30px;margin-top:10px;opacity:.95">{SC.esc(sub)}</div>'
             f'<div style="display:inline-block;margin-top:20px;padding:8px 18px;border-radius:999px;background:rgba(255,255,255,.2);'
             f'font-weight:600;font-size:21px">{SC.esc(pill)}</div></div>'
             f'<div style="position:absolute;left:600px;top:70px;transform:rotate(-8deg)">{p2}</div>'
             f'<div style="position:absolute;left:770px;top:40px;transform:rotate(6deg)">{p1}</div>')
    return page(inner, 1024, 500)

async def render(jobs):
    async with async_playwright() as p:
        b = await p.chromium.launch()
        ctx = await b.new_context()
        for html, path, w, h in jobs:
            pg = await ctx.new_page()
            await pg.set_viewport_size({'width': w, 'height': h})
            tmp = OUT / '_tmp.html'
            tmp.write_text(html, encoding='utf-8')
            await pg.goto(f'file://{tmp}')
            await pg.evaluate('document.fonts.ready')
            await pg.wait_for_timeout(150)
            await pg.screenshot(path=str(path), clip={'x': 0, 'y': 0, 'width': w, 'height': h})
            await pg.close()
        await b.close()

def flatten(path):
    im = Image.open(path)
    if im.mode != 'RGB':
        bg = Image.new('RGB', im.size, (255, 255, 255))
        bg.paste(im, mask=im.split()[-1] if im.mode in ('RGBA', 'LA') else None)
        bg.save(path, optimize=True)

if __name__ == '__main__':
    which = sys.argv[1:] or ['all']
    jobs = []
    for lang in ['es', 'pt', 'en']:
        d = OUT / lang
        d.mkdir(parents=True, exist_ok=True)
        for i, (key, title, sub) in enumerate(CAPTIONS[lang], start=1):
            if 'all' in which or key in which or lang in which:
                jobs.append((screenshot_html(lang, key, title, sub), d / f'{i}_{key}.png', 1080, 1920))
        if 'all' in which or 'feature' in which or lang in which:
            jobs.append((feature_html(lang), d / 'feature_graphic.png', 1024, 500))
    asyncio.run(render(jobs))
    for _, path, _, _ in jobs:
        flatten(path)
    print('rendered', len(jobs))
