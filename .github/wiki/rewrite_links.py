from pathlib import Path
import sys


ROOT = Path(sys.argv[1])

REPLACEMENTS = {
    "(Home.md)": "(Home)",
    "(../Home.md)": "(Home)",
    "(Installation.md)": "(Installation)",
    "(../Installation.md)": "(Installation)",
    "(Configuration.md)": "(Configuration)",
    "(../Configuration.md)": "(Configuration)",
    "(Commands.md)": "(Commands)",
    "(../Commands.md)": "(Commands)",
    "(Permissions.md)": "(Permissions)",
    "(../Permissions.md)": "(Permissions)",
    "(User-Interfaces.md)": "(User-Interfaces)",
    "(../User-Interfaces.md)": "(User-Interfaces)",
    "(Troubleshooting.md)": "(Troubleshooting)",
    "(../Troubleshooting.md)": "(Troubleshooting)",
    "(es/Inicio.md)": "(es-home)",
    "(es/Instalacion.md)": "(es-installation)",
    "(es/Configuracion.md)": "(es-configuration)",
    "(es/Comandos.md)": "(es-commands)",
    "(es/Permisos.md)": "(es-permissions)",
    "(es/Interfaces.md)": "(es-interfaces)",
    "(es/Solucion-de-problemas.md)": "(es-troubleshooting)",
    "(Inicio.md)": "(es-home)",
    "(Instalacion.md)": "(es-installation)",
    "(Configuracion.md)": "(es-configuration)",
    "(Comandos.md)": "(es-commands)",
    "(Permisos.md)": "(es-permissions)",
    "(Interfaces.md)": "(es-interfaces)",
    "(Solucion-de-problemas.md)": "(es-troubleshooting)",
    "(../../ROADMAP.md)": "(Roadmap)",
    "(../../../ROADMAP.md)": "(Roadmap)",
}


for page in ROOT.glob("*.md"):
    text = page.read_text(encoding="utf-8")
    for old, new in REPLACEMENTS.items():
        text = text.replace(old, new)
    page.write_text(text, encoding="utf-8", newline="\n")
