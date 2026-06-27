from pathlib import Path
import sys


ROOT = Path(sys.argv[1])

REPLACEMENTS = {
    "(Home.md)": "(Home)",
    "(../Home.md)": "(Home)",
    "(Installation.md)": "(Installation)",
    "(Configuration.md)": "(Configuration)",
    "(Commands.md)": "(Commands)",
    "(Permissions.md)": "(Permissions)",
    "(User-Interfaces.md)": "(User-Interfaces)",
    "(Troubleshooting.md)": "(Troubleshooting)",
    "(es/Inicio.md)": "(Inicio)",
    "(Inicio.md)": "(Inicio)",
    "(Instalacion.md)": "(Espanol-Instalacion)",
    "(Configuracion.md)": "(Espanol-Configuracion)",
    "(Comandos.md)": "(Espanol-Comandos)",
    "(Permisos.md)": "(Espanol-Permisos)",
    "(Interfaces.md)": "(Espanol-Interfaces)",
    "(Solucion-de-problemas.md)": "(Espanol-Solucion-de-problemas)",
    "(../../ROADMAP.md)": "(Roadmap)",
    "(../../../ROADMAP.md)": "(Roadmap)",
}


for page in ROOT.glob("*.md"):
    text = page.read_text(encoding="utf-8")
    for old, new in REPLACEMENTS.items():
        text = text.replace(old, new)
    page.write_text(text, encoding="utf-8", newline="\n")
