import requests
import logging
from logging.config import dictConfig
from ..util.log_config import LogConfig


dictConfig(LogConfig().dict())

logger = logging.getLogger("mycoolapp")


def post_message(url, payload, timeout=20):

    try:
        r = requests.post(url, json=payload, timeout=timeout)
        if r.status_code == 200:
            return
        else:
            r.raise_for_status()
    except requests.RequestException as e:
        logger.error(str(e) + " during request at url: " + str(url))
        raise e

