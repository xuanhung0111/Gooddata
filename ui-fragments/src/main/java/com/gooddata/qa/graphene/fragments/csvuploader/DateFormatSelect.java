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
        return "button.button-dropdown.s-format-dropdown";
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
        getElement(".s-" + type.getKey()).click();
        waitForSelectionIsApplied(name);
        return this;
    }

    public AbstractReactDropDown selectFormat(DateFormat format) {
        ensureDropdownOpen();
        getElement(".s-" + format.getKey()).click();
        waitForSelectionIsApplied(format.getVisibleText());
        return this;
    }

    public String getFormatSelection() {
        return getDropdownButton().getAttribute("title");
    }

    public static enum DateFormat {
        DAY_MONTH_YEAR_SEPARATED_BY_DOT("Day.Month.Year", "dddotMMdotyy"),
        DAY_MONTH_FULL_YEAR_SEPARATED_BY_DOT("Day.Month.Year", "dddotMMdotyyyy"),
        MONTH_DAY_YEAR_SEPARATED_BY_DOT("Month.Day.Year", "MMdotdddotyy"),
        YEAR_MONTH_DAY_SEPARATED_BY_DOT("Year.Month.Day", "yydotMMdotdd"),
        DAY_MONTH_YEAR_SEPARATED_BY_SLASH("Day/Month/Year", "ddslashMMslashyy"),
        DAY_MONTH_FULL_YEAR_SEPARATED_BY_SLASH("Day/Month/Year", "ddslashMMslashyyyy"),
        MONTH_DAY_YEAR_SEPARATED_BY_SLASH("Month/Day/Year", "MMslashddslashyy"),
        YEAR_MONTH_DAY_SEPARATED_BY_SLASH("Year/Month/Day", "yyslashMMslashdd"),
        DAY_MONTH_YEAR_SEPARATED_BY_HYPHEN("Day-Month-Year", "dddashMMdashyy"),
        DAY_MONTH_FULL_YEAR_SEPARATED_BY_HYPHEN("Day-Month-Year", "dddashMMdashyyyy"),
        MONTH_DAY_YEAR_SEPARATED_BY_HYPHEN("Month-Day-Year", "MMdashdddashyy"),
        YEAR_MONTH_DAY_SEPARATED_BY_HYPHEN("Year-Month-Day", "yydashMMdashdd"),
        DAY_MONTH_YEAR_SEPARATED_BY_SPACE("Day Month Year", "ddspaceMMspaceyy"),
        MONTH_DAY_YEAR_SEPARATED_BY_SPACE("Month Day Year", "MMspaceddspaceyy"),
        YEAR_MONTH_DAY_SEPARATED_BY_SPACE("Year Month Day", "yyspaceMMspacedd");

        private String formatByVisibleText;
        private String key;

        private DateFormat(String formatByVisibleText, String key) {
            this.formatByVisibleText = formatByVisibleText;
            this.key = key;
        }

        public String getVisibleText() {
            return formatByVisibleText;
        }

        public String getColumnType() {
            return format("Date (%s)", getVisibleText());
        }

        public String getKey() {
            return key;
        }
    }
}
