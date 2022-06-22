import React from "react";
import { Plugin } from "@openk9/rest-api";
import { WebResultItem } from "@openk9/search-frontend";

export const plugin: Plugin<WebResultItem> = {
  pluginId: "database-datasource",
  displayName: " DataSource",
  pluginServices: [
    {
      type: "DATASOURCE",
      displayName: "Database DataSource",
      driverServiceName: "io.openk9.plugins.database.driver.DatabasePluginDriver",
      initialSettings: `
        {
            "datasourceId": 1,
            "timestamp": 0,
            "startUrls": ["https://www.smc.it/"],
            "allowedDomains": ["smc.it"],
            "allowedPaths": [],
            "excludedPaths": ["/en"],
            "bodyTag": "body",
        	"maxLength": 50000
        }
      `,
    },
    {
      type: "ENRICH",
      displayName: "Database Async NER",
      serviceName:
        "io.openk9.plugins.database.enrichprocessor.AsyncDatabaseNerEnrichProcessor",
      initialSettings: `{
      "entities": ["person", "organization", "loc", "email"],
      "confidence": 0.80,
      "relations": [
            {
                "startType": "person",
                "endType": "organization",
                "name": "interacts_with"
            },
            {
                "startType": "person",
                "endType": "email",
                "name": "has_email"
            }
            ]
        }`,
    }
  ],
};