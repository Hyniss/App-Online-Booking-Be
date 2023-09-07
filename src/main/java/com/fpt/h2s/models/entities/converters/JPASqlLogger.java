package com.fpt.h2s.models.entities.converters;

import ananta.utility.StringEx;
import com.fpt.h2s.utilities.ExceptionPrinter;
import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.FormattedLogger;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JPASqlLogger extends FormattedLogger {
    
    private static final SqlFormatter.Formatter FORMATTER = SqlFormatter.of(Dialect.MySql);
    
    private static final FormatConfig FORMAT_OPTIONS = FormatConfig.builder()
        .indent("    ")
        .uppercase(true)
        .linesBetweenQueries(2)
        .maxColumnLength(100)
        .build();
    
    public void logException(final Exception e) {
        ExceptionPrinter.print(e);
    }
    
    @Override
    public void logText(final String query) {
    }
    
    public boolean isCategoryEnabled(final Category category) {
        return true;
    }
    
    public void logSQL(final int connectionId, final String now, final long elapsed, final Category category, final String prepared, final String sql, final String url) {
        if (StringEx.isNotBlank(sql)) {
            JPASqlLogger.log.info("\n" + JPASqlLogger.FORMATTER.format(sql, JPASqlLogger.FORMAT_OPTIONS));
        }
    }
    
}
