# buscador
Buscador: Java + Lucene + Tika + PHP

# TODO
- Listado de archivos (listado -d "dir" -tsv tsv.tsv -m "doc,docx" -bd "bd.txt")
  - Integramos mascara de archivo en comando? por defecto buscamos algo?
  - Listo, integrada mascara de archivos
  - Tenemos:
  - -d: Directorio a revisar (requerido)
  - -tsv: archivo a almacenar (requerido)
  - -m: mascara de archivos a buscar, separado por coma, pero sin espacio (no requerido, por defecto: doc,docx)
  - -bd: *Blacklist* de directorios, archivo txt con listado, separado por entre, de directorio que no nos interesan (no requerido, por defecto: ningun *blacklist*).
- Union de distintos archivos para listado general (ej. cuando tenemos dos directorio de interes)
- diferencias de archivos (diferencia -1 l1.tsv -2 l2.tsv -3 l3.tsv)
- extraccion de datos para Lucene (extraccion -l l3.tsv)
- busqueda (buscar -d /dir-lucene/ -b [palabras busqueda base64])


# Estado
funcionalidad: 80%
## Lo Bueno
* Funciona: busca en indice
* Ya tiene un port a webservice basico (PHP+Java)

## Lo Malo
* Buscar en indice lucene antiguo
* proceso de indexacion completamente manual
* nula automatizacion de actualizaciones


# Ideas varias
Que usamos para cada cosa
## Listado de archivos
Java
## Comparacion de  listados
Java
## Extraccion de datos
Apache POI
Apache Tika
## Indexacion
Lucene
## Interfaz de busqueda
PHP+HTML