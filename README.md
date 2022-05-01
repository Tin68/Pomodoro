# Pomodoro
App desarrollo Pomodoro

Esta es mi app. La he llamado Pomy.
No está finalizada del todo, he encontrado algunos fallos que no he conseguido solucionar aun.

La App funciona tanto en primer plano como minimizada

*EN PRIMER PLANO

En la primera imagen (1) se puede ver la estructura:
![Image text](https://github.com/Tin68/Pomy/blob/main/ImgReadme/Pomy_01.png)

- Tiene una barra de acciones donde se puede elegir el tiempo de descanso largo (Imagen 2)
![Image text](https://github.com/Tin68/Pomy/blob/main/ImgReadme/Pomy_02.png)

- Un imput text donde se pondrá el nombre de la tarea que se va a realizar
- Los botones para play, historial (activo solo cuando la app esta parada), stop, pause y next
- Y una tabla con los dos últimas sesiones de pomodoro. (Creo que la tabla tenía que ir a parte, pero me quedaba la pantalla muy vacía así que hay las puse.

![Image text](https://github.com/Tin68/Pomy/blob/main/ImgReadme/Pomy_03.png)
(Imagen 3) Al pulsar sobre el botón de histórico pasa a otra activity donde se muestra todo el listado de mayor a menor como se pedía. Todos estos datos los guarda en una base de datos Sqlite (Intente gestionarlo con ROOM, pero se me complicó un poco y al final lo hice a pelo), en esta base también guardo el tiempo de descanso largo.

![Image text](https://github.com/Tin68/Pomy/blob/main/ImgReadme/Pomy_04.png)
(Imagen 4) Como se pedía hasta que no se introduce una tarea no se habilita el botón de Play, deshabilitándose el del histórico en este momento.

![Image text](https://github.com/Tin68/Pomy/blob/main/ImgReadme/Pomy_05.png)
(Imagen 5) Pulsando el play comienza el Pomodoro, habilitándose los botones correspondientes y deshabilitándose el de play. El contador numérico es regresivo en los dos casos (tarea y descanso), pero el circular-bar me la he planteado como una barra de vida de un juego. Cuando estamos en Tarea es de color rojo y decrece según pasa el tiempo (Imagen 5) y el del descanso de color verde y crece (Imagen 6 y 7).

![Image text](https://github.com/Tin68/Pomy/blob/main/ImgReadme/Pomy_06.png)
![Image text](https://github.com/Tin68/Pomy/blob/main/ImgReadme/Pomy_07.png)

Entre tarea - descanso suena unas campanillas y he puesto un Toast (seguramente sacare este último, porque si no estás mirando la pantalla, ni lo ves) y entre descanso - tarea suena una sirena antiaérea.

![Image text](https://github.com/Tin68/Pomy/blob/main/ImgReadme/Pomy_08.png)
(Imagen 8) Cuando se pulsa el botón Stop, se detiene la aplicación, se actualiza el estado de los botones y guarda el trabajo en la BD (Imagen 9 se puede ver la lista actualizada) y en la tabla inferior se muestran las dos últimas tareas.
![Image text](https://github.com/Tin68/Pomy/blob/main/ImgReadme/Pomy_09.png)


*SEGUNDO PLANO
![Image text](https://github.com/Tin68/Pomy/blob/main/ImgReadme/Pomy_10.png)
Al pasar a segundo plano suena sonido de alarma y aparece el icono en la zona de notificaciones (Imagen10)

![Image text](https://github.com/Tin68/Pomy/blob/main/ImgReadme/Pomy_11.png)
En esta zona cuando se abre (Imagen11), se puede ver mediante un texto el tramo que está corriendo (Tarea o descanso), el tiempo que queda para finalizar el mismo. Y los botones para su manejo. Igual que en primer plano cuando cambia de tarea - descanso o descanso - tarea suenan los mismos sonidos de aviso.

Le he quitado el botón de stop, porque desde aquí no era capaz de guardar la información en la BD. Al pulsar sobre la zona donde no están los botones vuelve a la app en primer plano, pero también hace cosas raras (como si estuviesen corriendo dos timer a la vez) que tengo que investigar.

Pues esto es todo. Un poco extenso porque no tengo muy claro si seré capaz de subirlo a la red. Así al menos se puede ver cómo funciona.

He tenido un montón de problemas pero creo que me ha quedado algo interesante.
