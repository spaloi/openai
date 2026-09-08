package com.soumen.openai.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;


@Component
public class TimeTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeTool.class);

    @Tool(name ="getCurrentLocalTime",description = "Get the current time in user's timezone ")
    String getCurrentTime(){
        LOGGER.info("returning current time in user's timezone");
        return LocalTime.now().toString();
    }
    @Tool(name ="getCurrentTime",description = "Get the current time in user's timezone ")
    String getCurrentZoneTime(@ToolParam(description = "Value representing timeZone") String timeZone){
        LOGGER.info("returning current time to the given timezone {}",timeZone);
        return LocalTime.now(ZoneId.of(timeZone)).toString();
    }
}
