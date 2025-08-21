{{/*
Expand the name of the chart.
*/}}
{{- define "am-trade-management.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "am-trade-management.fullname" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "am-trade-management.labels" -}}
app.kubernetes.io/name: {{ include "am-trade-management.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "am-trade-management.selectorLabels" -}}
app.kubernetes.io/name: {{ include "am-trade-management.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Infrastructure service names
*/}}
{{- define "am-trade-management.postgresql.fullname" -}}
{{- .Values.postgresql.fullname }}
{{- end }}

{{- define "am-trade-management.influxdb.fullname" -}}
{{- .Values.influxdb.url }}
{{- end }}

{{- define "am-trade-management.kafka.fullname" -}}
{{- .Values.kafka.bootstrapServers }}
{{- end }}

{{- define "am-trade-management.zookeeper.fullname" -}}
{{- .Values.kafka.zookeeper.connect }}
{{- end }}
