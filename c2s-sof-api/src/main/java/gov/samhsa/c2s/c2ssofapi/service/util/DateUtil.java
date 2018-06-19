package gov.samhsa.c2s.c2ssofapi.service.util;

import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

@Slf4j
public class DateUtil {

    public static Date convertStringToDate(String dateString) throws ParseException {
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        if (dateString != null) {
            return format.parse(dateString);
        }
        return null;
    }


    public static Date convertStringToDateTime(String dateString) throws ParseException {
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z", Locale.US);
        if (dateString != null) {
            return format.parse(dateString);
        }
        return null;
    }

    public static LocalDate convertDateToLocalDate(Date date) {
        //the system default time zone will be appended
        ZoneId defaultZoneId = ZoneId.systemDefault();

        //1. Convert Date -> Instant
        Instant instant = date.toInstant();

        //2. Instant + system default time zone + toLocalDate() = LocalDate

        return instant.atZone(defaultZoneId).toLocalDate();
    }

    public static String convertDateToString(Date date) {
        DateFormat df = new SimpleDateFormat("MM/dd/YYYY", Locale.US);

        if (date != null) {
            return df.format(date);
        }
        return "";
    }

    public static String convertDateTimeToString(Date date) {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z", Locale.US);

        if (date != null) {
            return df.format(date);
        }
        return "";
    }

    public static String convertLocalDateTimeToString(LocalDateTime date) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss", Locale.US);

        if (date != null) {
            return date.format(df);
        }
        return "";
    }

    public static String convertLocalDateToString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        if (date != null) {
            return formatter.format(date);
        }
        return "";
    }

    public static LocalDateTime convertUTCDateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
    }

    public static Date convertLocalDateTimeToUTCDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.of("UTC")).toInstant());
    }

    public static LocalDateTime convertDateToLocalDateTime(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Returns true if endDate is after startDate or if startDate equals endDate.
     * Returns false if either value is null.  If equalOK, returns true if the
     * dates are equal.
     **/
    public static boolean isValidDateRange(Date startDate, Date endDate, boolean equalOK) {
        // false if either value is null
        if (startDate == null || endDate == null) {
            return false;
        }

        if (equalOK) {
            // true if they are equal
            if (startDate.equals(endDate)) {
                return true;
            }
        }

        // true if endDate after startDate
        return endDate.after(startDate);

    }

    /**
     * Returns true if endDateTime is after startDateTime or if startDateTime equals endDateTime.
     * Returns false if either value is null.  If equalOK, returns true if the
     * datesTimes are equal.
     **/
    public static boolean isValidDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime, boolean equalOK) {
        // false if either value is null
        if (startDateTime == null || endDateTime == null) {
            return false;
        }

        if (equalOK) {
            // true if they are equal
            if (startDateTime.equals(endDateTime)) {
                return true;
            }
        }

        // true if endDateTime after startDateTime
        return endDateTime.isAfter(startDateTime);

    }

    public static String convertLocalDateTimeToHumanReadableFormat(LocalDateTime dateTime) {
        int hour;
        String min;

        if (dateTime.getMinute() == 0) {
            min = ":00";
        } else if (dateTime.getMinute() < 10) {
            min = ":0" + dateTime.getMinute();
        } else min = ":" + dateTime.getMinute();

        if (dateTime.getHour() == 0) {
            return "00" + min + " AM";
        }
        if (dateTime.getHour() < 12) {
            hour = dateTime.getHour();
            return hour + min + " AM";
        }
        if (dateTime.getHour() == 12) {
            return "12" + min + " PM";
        }
        hour = dateTime.getHour() - 12;
        return hour + min + " PM";
    }

}
