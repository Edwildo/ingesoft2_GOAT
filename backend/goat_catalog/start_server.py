"""Script para iniciar el servidor con PYTHONPATH configurado correctamente."""

import sys
from pathlib import Path

# Agregar src al PYTHONPATH
src_path = Path(__file__).parent / "src"
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

if __name__ == "__main__":
    import uvicorn
    
    # Ahora podemos importar el módulo correctamente
    uvicorn.run(
        "goat_catalog.main:app",
        host="0.0.0.0",
        port=8082,
        reload=True,
    )

