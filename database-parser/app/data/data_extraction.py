import threading
import logging
from datetime import datetime
from logging.config import dictConfig
import json
import requests
from sqlalchemy import create_engine
from .util.log_config import LogConfig

dictConfig(LogConfig().dict())


class DataExtraction(threading.Thread):

    def __init__(self, dialect, driver, user, password, host,
                 port, db, query, timestamp, datasource_id, ingestion_url):

        super(DataExtraction, self).__init__()
        self.dialect = dialect
        self.driver = driver
        self.user = user
        self.password = password
        self.host = host
        self.port = port
        self.db = db
        self.query = query
        self.timestamp = timestamp
        self.datasource_id = datasource_id
        self.ingestion_url = ingestion_url

        self.url_extract = self.dialect + "+" + self.driver + "://" + self.user + ":" + self.password + "@" + self.host + ":" + self.port + "/" + self.db

        self.status_logger = logging.getLogger("crm-parser")

    def manage_data(self, results):
        row_numbers = 0
        end_timestamp = datetime.utcnow().timestamp() * 1000

        self.status_logger.info("Posting contacts")

        for row in results:
            try:

                row_values = json.dumps(row)

                datasource_payload = {"contact": row_values}

                raw_content = ""

                payload = {
                    "datasourceId": self.datasource_id,
                    "contentId": "",
                    "parsingDate": int(end_timestamp),
                    "rawContent": raw_content,
                    "datasourcePayload": datasource_payload,
                    "resources": {
                        "binaries": []
                    }
                }

                try:
                    self.status_logger.info(datasource_payload)
                    # post_message(self.ingestion_url, payload, self.timeout)
                    row_numbers = row_numbers + 1
                except requests.RequestException:
                    self.status_logger.error("Problems during posting of row with id: ")
                    continue

            except json.decoder.JSONDecodeError:
                continue

        self.status_logger.info("Posting ended")
        self.status_logger.info("Have been posted " + str(row_numbers) + " rows")

        return row_numbers

    def extract_recent(self):
        try:
            engine = create_engine(self.url_extract)
            with engine.connect as conn:
                results = conn.execute(self.query)
                self.manage_data(results)

        except requests.RequestException:
            self.status_logger.error("No contact extracted. Extraction process aborted.")
            return

        self.status_logger.info("Extraction ended")

        return
