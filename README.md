
# Time is Life - 道路避讓及時警示系統
<img src="./IMG/01.png" height="440" width="250" align = "left">

## Description
台灣是一個人口密度高的國家，相對的機、汽車密度也高，道路上的車流量非常可觀，也因此產生救護車在返往醫院和救災現場時，因為許多原因而造成救災的延誤，例如前方車輛沒有注意到救護車即將經過、道路前方出現車禍、或是道路上的駕駛人彼此沒有一定的避讓共識等等[1]。近年來新聞上出現駕駛人不禮讓即將經過的救護車情形屢見不鮮，甚至有些案例是因延誤搶救時間，而造成車上病患不治的情況出現。儘管已有部分法條規範駕駛人在面對救護車時應如何進行避讓行為，但仍有大部分的駕駛人缺乏足夠的避讓行為知識而不知所措。

基於上述問題，我們決定設計與實作一款整合Google Map API導航技術與路徑判斷功能的道路避讓即時警示系統，在車輛駕駛將要行駛時使用APP開啟偵測功能，定時判斷是否有救護車即將經過駕駛人當前所在位置進而發出訊息提醒。

## Introduction
[Time is Life - 道路避讓及時警示系統](https://www.youtube.com/watch?v=iOwsqZYc7mw&feature=youtu.be)
<br /><br />

## System Architecture
<img src="./IMG/05.png" height="400" width="1000" align = "center">

系統架構總共有一個伺服器與兩種不同類型的客戶端，分別為`救護車客戶端`與`一般駕駛客戶端`。

- `救護車客戶端`:我們提供救護車即將經過的提醒服務，希望所有車輛和救護車上皆能安裝本系統的APP(行動裝置應用程式)。當救護車端即將要載送傷患至鄰近醫院時，會將救護車自己當前所在的位置告知伺服器端，由伺服器回傳附近醫院的急診室狀況給救護車端進行判斷前往何處醫院。選擇完畢後，伺服器端會將救護車當前位置與欲前往的醫院位置進行路徑規劃，並將規劃好的路徑資訊傳給所有的客戶端(包含救護車客戶端與一般車輛客戶端)，要進行救援的救護車客戶端可以依照伺服器傳回的路徑導航。

- `一般駕駛客戶端`:一般車輛客戶端會向伺服器端索取救護車的導航路徑資訊，並根據本身所在位置判斷自己是否位於該導航路徑附近，如果是的話就會發出警報，提醒駕駛者及早進行道路避讓。

## System flow

## How to Start
`Google_API_O_I_O_I` folder is android studio folder, just open android studio and import it.

`Server` is an eclipse project.

## Use Language & Packages
- Android Studio / Eclipse
- Java(EE) / Javascript / Html
- WebSocket IO API / Google Map API

### Environment require
1. tomcat v7.0
2. J2EE
3. JDK v1.7

## Detail
You can see PPT in the folder, and get more information from it.



