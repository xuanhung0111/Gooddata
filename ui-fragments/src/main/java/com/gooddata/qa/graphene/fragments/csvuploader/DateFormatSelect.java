package com.gooddata.qa.graphene.fragments.csvuploader;

import static java.lang.String.format;
import static java.util.Objects.isNull;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import com.gooddata.qa.utils.EnumUtils;

public class DateFormatSelect extends AbstractReactDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".dropdown-list";
    }

    @Override
    protected String getDropdownButtonCssSelector() {
        return "button.button-dropdown[data-reactid*=date-format]";
    }

    @Override
    protected String getSearchInputCssSelector() {
        return null;
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-item:not(.is-header)";
    }

    @Override
    protected void waitForPickerLoaded() {
    }

    @Override
    public AbstractReactDropDown selectByName(String name) {
        ensureDropdownOpen();
        final DateFormat type = EnumUtils.lookup(name, DateFormat.class, null, "getVisibleText");
        if (isNull(type)) {
            throw new IllegalArgumentException("Unknown date format type: " + name);
        }
        getElement(format("[data-reactid*='%s']", type.getDataReactId())).click();
        waitForSelectionIsApplied(name);
        return this;
    }

    public String getFormatSelection() {
        return getDropdownButton().getAttribute("title");
    }

    public static enum DateFormat {
        DAY_MONTH_YEAR_SEPARATED_BY_DOT("Day.Month.Year", "dd=1MM=1yy"),
        MONTH_DAY_YEAR_SEPARATED_BY_DOT("Month.Day.Year", "MM=1dd=1yy"),
        YEAR_MONTH_DAY_SEPARATED_BY_DOT("Year.Month.Day", "yy=1MM=1dd"),
        DAY_MONTH_YEAR_SEPARATED_BY_SLASH("Day/Month/Year", "dd/MM/yy"),
        MONTH_DAY_YEAR_SEPARATED_BY_SLASH("Month/Day/Year", "MM/dd/yy"),
        YEAR_MONTH_DAY_SEPARATED_BY_SLASH("Year/Month/Day", "yy/MM/dd"),
        DAY_MONTH_YEAR_SEPARATED_BY_HYPHEN("Day-Month-Year", "dd-MM-yy"),
        MONTH_DAY_YEAR_SEPARATED_BY_HYPHEN("Month-Day-Year", "MM-dd-yy"),
        YEAR_MONTH_DAY_SEPARATED_BY_HYPHEN("Year-Month-Day", "yy-MM-dd"),
        DAY_MONTH_YEAR_SEPARATED_BY_SPACE("Day Month Year", "dd MM yy"),
        MONTH_DAY_YEAR_SEPARATED_BY_SPACE("Month Day Year", "MM dd yy"),
        YEAR_MONTH_DAY_SEPARATED_BY_SPACE("Year Month Day", "yy MM dd");

        private String formatByVisibleText;
        private String dataReactId;

        private DateFormat(String formatByVisibleText, String dataReactId) {
            this.formatByVisibleText = formatByVisibleText;
            this.dataReactId = dataReactId;
        }

        public String getVisibleText() {
            return formatByVisibleText;
        }

        public String getColumnType() {
            return format("Date (%s)", getVisibleText());
        }

        public String getDataReactId() {
            return dataReactId;
        }
    }
}
