FROM python:3.8

COPY ./database-parser/requirements.txt /requirements.txt

# install system dependencies
RUN apt-get update \
  && apt-get -y install gcc \
  && apt-get -y install g++ \
  && apt-get -y install unixodbc unixodbc-dev \
  && apt-get clean

RUN pip install -r requirements.txt

COPY ./database-parser/app /app

WORKDIR /app

CMD ["uvicorn", "main:app", "--reload", "--host", "0.0.0.0", "--port", "5000"]

