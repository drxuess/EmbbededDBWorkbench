/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlclient.model;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.text.Font;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.PlainTextChange;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;

/**
 *
 * @author Morgan
 */
public class SQLCodeArea {
    //Filtering Variables
    private static final String[] KEYWORDS = new String[] {
        "ADD" ,"ALL" ,"ALLOCATE" ,"ALTER" ,"AND" ,"ANY" ,"ARE" ,"AS" ,"ASC" ,
        "ASSERTION" ,"AT" ,"AUTHORIZATION" ,"BEGIN" ,"BETWEEN" ,"BIT" ,
        "BOOLEAN" ,"BOTH" ,"BY" ,"CALL" ,"CASCADE" ,"CASCADED" ,"CASE" ,"CAST" ,
        "CHAR" ,"CHARACTER" ,"CHECK" ,"CLOSE" ,"COLLATE" ,"COLLATION" ,"COLUMN" ,
        "COMMIT" ,"CONNECT" ,"CONNECTION" ,"CONSTRAINT" ,"CONSTRAINTS" ,"CONTINUE" ,
        "CONVERT" ,"CORRESPONDING" ,"COUNT" ,"CREATE" ,"CURRENT" ,"CURRENT_DATE" ,
        "CURRENT_TIME" ,"CURRENT_TIMESTAMP" ,"CURRENT_USER" ,"CURSOR" ,"DEALLOCATE" ,
        "DEC" ,"DECIMAL" ,"DECLARE" ,"DEFERRABLE" ,"DEFERRED" ,"DELETE" ,"DESC" ,
        "DESCRIBE" ,"DIAGNOSTICS" ,"DISCONNECT" ,"DISTINCT" ,"DOUBLE" ,"DROP" ,"ELSE" ,
        "END" ,"ENDEXEC" ,"ESCAPE" ,"EXCEPT" ,"EXCEPTION" ,"EXEC" ,"EXECUTE" ,"EXISTS" ,
        "EXPLAIN" ,"EXTERNAL" ,"FALSE" ,"FETCH" ,"FIRST" ,"FLOAT" ,"FOR" ,"FOREIGN" ,
        "FOUND" ,"FROM" ,"FULL" ,"FUNCTION" ,"GET" ,"GET_CURRENT_CONNECTION" ,"GLOBAL" ,
        "GO" ,"GOTO" ,"GRANT" ,"GROUP" ,"HAVING" ,"IDENTITY" ,"IMMEDIATE" ,
        "IN" ,"INDICATOR" ,"INITIALLY" ,"INNER" ,"INOUT" ,"INPUT" ,"INSENSITIVE" ,
        "INSERT" ,"INT" ,"INTEGER" ,"INTERSECT" ,"INTO" ,"IS" ,"ISOLATION" ,"JOIN" ,
        "KEY" ,"LAST" ,"LEFT" ,"LIKE" ,"LONGINT" ,"MATCH" ,
        "NATIONAL" ,"NATURAL" ,"NCHAR" ,"NVARCHAR" ,"NEXT" ,"NO" ,
        "NOT" ,"NULL" ,"NUMERIC" ,"OF" ,"ON" ,"ONLY" ,"OPEN" ,"OPTION" ,
        "OR" ,"ORDER" ,"OUT" ,"OUTER" ,"OUTPUT" ,"OVERLAPS" ,"PAD" ,"PARTIAL" ,"PREPARE" ,
        "PRESERVE" ,"PRIMARY" ,"PRIOR" ,"PRIVILEGES" ,"PROCEDURE" ,"PUBLIC" ,"READ" ,
        "REAL" ,"REFERENCES" ,"RELATIVE" ,"RESTRICT" ,"REVOKE" ,"RIGHT" ,"ROLLBACK" ,
        "ROWS" ,"SCHEMA" ,"SCROLL" ,"SECOND" ,"SELECT" ,"SESSION_USER" ,
        "SET" ,"SMALLINT" ,"SOME" ,"SPACE" ,"SQL" ,"SQLCODE" ,"SQLERROR" ,"SQLSTATE" ,
        "SYSTEM_USER" ,"TABLE" ,"TEMPORARY" ,"TIMEZONE_HOUR" ,
        "TIMEZONE_MINUTE" ,"TO" ,"TRAILING" ,"TRANSACTION" ,"TRANSLATE" ,"TRANSLATION" ,
        "TRUE" ,"UNION" ,"UNIQUE" ,"UNKNOWN" ,"UPDATE" ,"UPPER" ,"USER" ,"USING" ,
        "VALUES" ,"VARCHAR" ,"VARYING" ,"VIEW" ,"WHENEVER" ,"WHERE" ,"WITH" ,"WORK" ,
        "WRITE" ,"XML","YEAR"
    };
    
    private static final String[] FUNCTIONS = new String[] {
        "ABS", "ABSVAL", "ACOS", "ASIN", "ATAN", "AVG", "BIGINT", "CAST", "CEIL", 
        "CEILING", "COS", "COUNT", "NULLIF", "DATE", "DAY", "DEGREES", 
        "EXP", "FLOOR", "HOUR", "LENGTH", "LN", "LOG", "LOG10", "LOCATE", 
        "LCASE", "LOWER", "LTRIM", "MAX", "MIN", "MINUTE", "MOD", "MONTH", "PI", 
        "RADIANS", "RTRIM", "SECOND", "SIN", "SQRT", "SUBSTR", "SUM", 
        "TAN", "TIME", "TIMESTAMP", "UCASE", "UPPER", "USER", "XMLEXISTS", 
        "XMLPARSE", "XMLSERIALIZE", "VARCHAR", "YEAR"
    };
    
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String FUNCTION_PATTERN = "\\b(" + String.join("|", FUNCTIONS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\'([^\'\"\\\\]|\\\\.)*\'";
    private static final String COMMENT_PATTERN = "--[^\n]*" + "|" + "#[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    
    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<FUNCTION>" + FUNCTION_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    //CodePane variable
    private CodeArea codeArea;
    
    public SQLCodeArea(){
        codeArea = new CodeArea();
        
        //codeArea.setStyle("-fx-font: 12px \"Consolas\"");
        
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            codeArea.setStyleSpans(0, computeHighlighting(newText));
        });
    }
    
    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("FUNCTION") != null ? "function":
                    matcher.group("PAREN") != null ? "paren" :
                    matcher.group("BRACE") != null ? "brace" :
                    matcher.group("BRACKET") != null ? "bracket" :
                    matcher.group("SEMICOLON") != null ? "semicolon" :
                    matcher.group("STRING") != null ? "string" :
                    matcher.group("COMMENT") != null ? "comment" :
                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
    
    public CodeArea getCodeArea(){
        return codeArea;
    }
}
