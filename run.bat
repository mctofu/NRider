powercfg /setactive "Presentation"
java -Djava.library.path=lib\rxtx-2.1-7-bins-r2 -classpath ".\out\production\NRider;.\lib\rxtx-2.1-7-bins-r2\RXTXcomm.jar;.\lib\rxtx-2.1-7-bins-r2;.\lib\log4j-1.2.15.jar" nrider.NRider %1
powercfg /setactive "Home/Office Desk"