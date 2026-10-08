import asyncio, sys
from playwright.async_api import async_playwright

async def render_html(html, path, w, h, scale=1):
    async with async_playwright() as p:
        b = await p.chromium.launch()
        pg = await b.new_page(viewport={"width": w, "height": h}, device_scale_factor=scale)
        await pg.set_content(html, wait_until="networkidle")
        await pg.evaluate("document.fonts.ready")
        await pg.screenshot(path=path, omit_background=True)
        await b.close()

async def render_many(jobs):
    async with async_playwright() as p:
        b = await p.chromium.launch()
        for html, path, w, h in jobs:
            pg = await b.new_page(viewport={"width": w, "height": h})
            await pg.set_content(html, wait_until="networkidle")
            await pg.evaluate("document.fonts.ready")
            await pg.wait_for_timeout(100)
            await pg.screenshot(path=path, omit_background=True)
            await pg.close()
        await b.close()
