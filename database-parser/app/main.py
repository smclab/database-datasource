import logging
import os
import threading
from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional
from data.data_extraction import DataExtraction

app = FastAPI()

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
    ingestion_url = "http://ingestion:8080/v1/ingestion/"

delete_url = os.environ.get("DELETE_URL")
if delete_url is None:
    delete_url = "http://index-writer:8080/v1/delete-data-documents"

logger = logging.getLogger("uvicorn.access")


class DatabaseRequest(BaseModel):
    dialect: str
    driver: str
    user: str
    password: str
    host: str
    port: str
    db: str
    table: str
    columns: Optional[List[str]]
    where: Optional[str]
    timestamp: int
    datasourceId: int
    scheduleId: str


@app.post("/getData")
def get_data(request: DatabaseRequest):
    request = request.dict()

    dialect = request["dialect"]
    driver = request["driver"]
    user = request["user"]
    password = request["password"]
    host = request["host"]
    port = request["port"]
    db = request["db"]
    table = request["table"]
    columns = request["columns"]
    where = request["where"]
    timestamp = request["timestamp"]
    datasource_id = request["datasourceId"]
    schedule_id = request['scheduleId']

    data_extraction = DataExtraction(dialect, driver, user, password,
                                     host, port, db, table, columns, where,
                                     timestamp, datasource_id, schedule_id, ingestion_url)

    thread = threading.Thread(target=data_extraction.extract_recent)
    thread.start()

    return "Extraction Started"
