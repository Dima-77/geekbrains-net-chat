"c:\Program Files\Java\jdk1.8.0_92\bin\javac" -encoding UTF-8 MainClass.java
md jar
"c:\Program Files\Java\jdk1.8.0_92\bin\jar" cvfe jar\Chat-server.jar MainClass *.class
md Class
move /Y *.class Class\
echo Создаём батничек для запуска в папке Class
echo chcp > Class\runJava.bat
echo "c:\Program Files\Java\jdk1.8.0_92\bin\java" MainClass >> Class\runJava.bat
echo pause >> Class\runJava.bat
pause