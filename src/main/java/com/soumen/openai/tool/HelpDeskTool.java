package com.soumen.openai.tool;

import com.soumen.openai.entity.HelpDeskTicket;
import com.soumen.openai.model.TicketRequest;
import com.soumen.openai.service.HelpDeskService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HelpDeskTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelpDeskTool.class);
    private final HelpDeskService helpDeskService;

    @Tool(name="createTicket" ,description = "create a support ticket", returnDirect = true)
    public String createTicket(@ToolParam(description = "Details to create a support tickets")
                              TicketRequest issue, ToolContext toolContext ){

          String userName = (String) toolContext.getContext().get("username");
          HelpDeskTicket saved = helpDeskService.createTicket(issue,userName);
        LOGGER.info("Ticket created successfully");
          return "Ticket created  with "+ saved.getId() + "successfully";
    }

    @Tool(description = "fetch the status of  tickets based on the provided username")
    public List<HelpDeskTicket> getTicketStatus(ToolContext toolContext ){
        String userName = (String) toolContext.getContext().get("username");
        LOGGER.info("Ticket fetched successfully");
       return helpDeskService.getTicketsByUsername(userName);
    }



}
