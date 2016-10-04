"c:\Program Files\Java\jdk1.8.0_92\bin\javac" FillSqlTable.java
md Class
move /Y *.class Class\
echo Создаём батничек для запуска в папке Class
echo chcp > Class\runJava.bat
echo "c:\Program Files\Java\jdk1.8.0_92\bin\java" FillSqlTable >> Class\runJava.bat
echo pause >> Class\runJava.bat
pause