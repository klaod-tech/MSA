#!/usr/bin/env bash
: ${HOST=localhost}
: ${PORT=8080}                                      # ★ ① 변경 (3장: 7001)
: ${PROD_ID_REVS_RECS=1}
: ${PROD_ID_NOT_FOUND=13}
: ${PROD_ID_NO_RECS=113}
: ${PROD_ID_NO_REVS=213}

function assertCurl() {

  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]
  then
    if [ "$httpCode" = "200" ]
    then
      echo "Test OK (HTTP Code: $httpCode)"
    else
      echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
    fi
  else
    echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
    echo  "- Failing command: $curlCmd"
    echo  "- Response Body: $RESPONSE"
    exit 1
  fi
}

function assertEqual() {

  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]
  then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    exit 1
  fi
}

function testUrl() {                                # ★ ② 추가
  url=$@                                            # ★ ② 추가
  if $url -ks -f -o /dev/null                       # ★ ② 추가
  then                                              # ★ ② 추가
    return 0                                        # ★ ② 추가
  else                                              # ★ ② 추가
    return 1                                        # ★ ② 추가
  fi;                                               # ★ ② 추가
}                                                   # ★ ② 추가

function waitForService() {                         # ★ ② 추가
  url=$@                                            # ★ ② 추가
  echo -n "Wait for: $url... "                      # ★ ② 추가
  n=0                                               # ★ ② 추가
  until testUrl $url                                # ★ ② 추가
  do                                                # ★ ② 추가
    n=$((n + 1))                                    # ★ ② 추가
    if [[ $n == 100 ]]                              # ★ ② 추가
    then                                            # ★ ② 추가
      echo " Give up"                               # ★ ② 추가
      exit 1                                        # ★ ② 추가
    else                                            # ★ ② 추가
      sleep 3                                       # ★ ② 추가
      echo -n ", retry #$n "                        # ★ ② 추가
    fi                                              # ★ ② 추가
  done                                              # ★ ② 추가
  echo "DONE, continues..."                         # ★ ② 추가
}                                                   # ★ ② 추가

set -e

echo "Start Tests:" `date`                          # ★ ③ 추가

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]                            # ★ ③ 추가
then                                                # ★ ③ 추가
  echo "Restarting the test environment..."         # ★ ③ 추가
  echo "$ docker compose down --remove-orphans"     # ★ ③ 추가
  docker compose down --remove-orphans              # ★ ③ 추가
  echo "$ docker compose up -d"                     # ★ ③ 추가
  docker compose up -d                              # ★ ③ 추가
fi                                                  # ★ ③ 추가

waitForService curl http://$HOST:$PORT/product-composite/$PROD_ID_REVS_RECS  # ★ ③ 추가

assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_REVS_RECS -s"
assertEqual $PROD_ID_REVS_RECS $(echo $RESPONSE | jq .productId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")

assertCurl 404 "curl http://$HOST:$PORT/product-composite/$PROD_ID_NOT_FOUND -s"
assertEqual "No product found for productId: $PROD_ID_NOT_FOUND" "$(echo $RESPONSE | jq -r .message)"

assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_NO_RECS -s"
assertEqual $PROD_ID_NO_RECS $(echo $RESPONSE | jq .productId)
assertEqual 0 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")

assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_NO_REVS -s"
assertEqual $PROD_ID_NO_REVS $(echo $RESPONSE | jq .productId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length")

assertCurl 422 "curl http://$HOST:$PORT/product-composite/-1 -s"
assertEqual "\"Invalid productId: -1\"" "$(echo $RESPONSE | jq .message)"

assertCurl 400 "curl http://$HOST:$PORT/product-composite/invalidProductId -s"
assertEqual "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

if [[ $@ == *"stop"* ]]                             # ★ ④ 추가
then                                                # ★ ④ 추가
    echo "We are done, stopping the test environment..."  # ★ ④ 추가
    echo "$ docker compose down"                    # ★ ④ 추가
    docker compose down                             # ★ ④ 추가
fi                                                  # ★ ④ 추가

echo "End, all tests OK:" `date`