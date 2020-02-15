FROM scratch
LABEL maintainer "Rintaro Okamura <rintaro.okamura@gmail.com>"

COPY valdcli /valdcli

ENTRYPOINT ["/valdcli"]
