# 검색 분석용 nori(한국어 형태소 분석) 플러그인을 포함한 Elasticsearch 이미지.
# 공식 이미지엔 nori가 기본 포함돼 있지 않아 빌드해서 GHCR에 올려 씀.
FROM docker.elastic.co/elasticsearch/elasticsearch:9.4.2

RUN bin/elasticsearch-plugin install --batch analysis-nori