//실시간으로 킥보드 gps 불러오기

var myHeaders = new Headers();
myHeaders.append("Accept", "application/json");
myHeaders.append("X-M2M-RI", "12345");
myHeaders.append("X-M2M-Origin", "SOrigin");

var requestOptions = {
    method: 'GET',
    headers: myHeaders,
    redirect: 'follow'
};


var string
var arr
var lat
var long

setInterval(function(){
    fetch("http://203.250.148.120:20519/Mobius/kick_off/data/gps/la", requestOptions)
    .then(response => response.json())
    .then(result => {
        string = result["m2m:cin"].con
        arr = string.split(" ")
        lat = Number(arr[1])
        long = Number(arr[2])
    })
    .then(result => {
        // 킥보드 위치 받아서 띄우기
        navigator.geolocation.getCurrentPosition(locationLoadSuccess,locationLoadError)
    })
    .catch(error => console.log('error', error));
}, 1000)

//////////////////////////////////////////////////////////////////////////////

// 그룹 데이터 불러오기

var pothole_ID = []
var buff_ID = []
var parking_ID = []

var lat
var long

var potholePositions = []
var bumpPositions = []
var parkingPositions = []
var circle

var myHeaders = new Headers();
myHeaders.append("Accept", "application/json");
myHeaders.append("X-M2M-RI", "12345");
myHeaders.append("X-M2M-Origin", "SOrigin");

var requestOptions = {
    method: 'GET',
    headers: myHeaders,
    redirect: 'follow'
};

fetch("http://203.250.148.120:20519/Mobius/kick_off/web_grp/fopt", requestOptions)
.then(response => response.json())
.then(result => {

    // 저장되어있는 모든 ID 뽑아오기
    for (var i = 0; i < result["m2m:agr"]["m2m:rsp"][0]["pc"]["m2m:uril"].length; i++)        {
        pothole_ID[i] = result["m2m:agr"]["m2m:rsp"][0]["pc"]["m2m:uril"][i].split("/")[5]
    }

    for (var i = 0; i < result["m2m:agr"]["m2m:rsp"][1]["pc"]["m2m:uril"].length; i++)        {
        buff_ID[i] = result["m2m:agr"]["m2m:rsp"][1]["pc"]["m2m:uril"][i].split("/")[5]
    } 
    for (var i = 0; i < result["m2m:agr"]["m2m:rsp"][3]["pc"]["m2m:uril"].length; i++)        {
        parking_ID[i] = result["m2m:agr"]["m2m:rsp"][3]["pc"]["m2m:uril"][i].split("/")[5]
    }   
})
.then(result => {
    var ID = [pothole_ID, buff_ID, parking_ID]
    console.log(ID)

    // 포트홀 position 저장
    for (var i = 0; i < ID[0].length; i++) {

        var myHeaders = new Headers();
        myHeaders.append("Accept", "application/json");
        myHeaders.append("X-M2M-RI", "12345");
        myHeaders.append("X-M2M-Origin", "SOrigin");

        var requestOptions = {
        method: 'GET',
        headers: myHeaders,
        redirect: 'follow'
        };
        var j = 0
        fetch("http://203.250.148.120:20519/Mobius/kick_off/map/pothole/gps/"+ ID[0][i], requestOptions)
        .then(response => response.json())
        .then(result => {
            lat = result["m2m:cin"]["con"].split(" ")[0]
            long = result["m2m:cin"]["con"].split(" ")[1]
            potholePositions.push(new kakao.maps.LatLng(lat, long))
        })
        .then(result => {
            // 방지턱 position 저장
            for (var j = 0; j < ID[1].length; j++) {

                var myHeaders = new Headers();
                myHeaders.append("Accept", "application/json");
                myHeaders.append("X-M2M-RI", "12345");
                myHeaders.append("X-M2M-Origin", "SOrigin");

                var requestOptions = {
                method: 'GET',
                headers: myHeaders,
                redirect: 'follow'
                };

                fetch("http://203.250.148.120:20519/Mobius/kick_off/map/speed_bump/gps/"+ ID[1][j], requestOptions)
                .then(response => response.json())
                .then(result => {

                    lat = result["m2m:cin"]["con"].split(" ")[0]
                    long = result["m2m:cin"]["con"].split(" ")[1]

                    bumpPositions.push(new kakao.maps.LatLng(lat, long))
                })
                .then(result => {
                    for (var k = 0; k < ID[2].length; k++) {
                        var myHeaders = new Headers();
                        myHeaders.append("Accept", "application/json");
                        myHeaders.append("X-M2M-RI", "12345");
                        myHeaders.append("X-M2M-Origin", "SOrigin");
            
                        var requestOptions = {
                        method: 'GET',
                        headers: myHeaders,
                        redirect: 'follow'
                        };
                        fetch("http://203.250.148.120:20519/Mobius/kick_off/map/parking_space/gps/"+ ID[2][k], requestOptions)
                        .then(response => response.json())
                        .then(result => {
                            lat = result["m2m:cin"]["con"].split(" ")[0]
                            long = result["m2m:cin"]["con"].split(" ")[1]

                            parkingPositions.push(new kakao.maps.LatLng(lat, long))
                        })
                        .then(result => {

                            createPotholeMarkers()
                            createBumpMarkers()
                            createParkingMarkers()

                            changeMarker("all")

                        })
                        .catch(error => console.log('error', error));
                    }
                })
                .catch(error => console.log('error', error));
            }
        })
        .catch(error => console.log('error', error));
    }

})
.catch(error => console.log('error', error));



////////////////////////////////////////////////////////////////////////////////////

// 맵 기본틀 띄우기

var container = document.getElementById('map'); //지도를 담을 영역의 DOM 레퍼런스

var options = { //지도를 생성할 때 필요한 기본 옵션
	center: new kakao.maps.LatLng(37.5511, 127.0738), //지도의 중심좌표
	level: 3 //지도의 레벨(확대, 축소 정도)
};

var map = new kakao.maps.Map(container, options); //지도 생성 및 객체 리턴


////////////////////////////////////////////////////////////////////////////////////

// 킥보드 위치 띄우기

function locationLoadSuccess(pos){
    // 현재 위치 받아오기
    //var currentPos = new kakao.maps.LatLng(pos.coords.latitude,pos.coords.longitude);

    // 킥보드 현재 위치 받아오기
    // 37.5518018 127.0736345

    //var currentPos = new kakao.maps.LatLng(lat, long);
    var currentPos = new kakao.maps.LatLng(37.5518018, 127.0736345);

    // 지도 이동(기존 위치와 가깝다면 부드럽게 이동)
    map.panTo(currentPos);
    
    var imageSrc = '../../assets/logo_img.png', // 마커이미지의 주소  
        imageSize = new kakao.maps.Size(26, 26), // 마커이미지 크기
        imageOption = {offset: new kakao.maps.Point(27, 69)}; // 마커이미지의 옵션
        
    // 마커의 이미지정보를 가지고 있는 마커이미지 생성
    var markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imageOption)

    // 마커를 생성합니다
    var marker = new kakao.maps.Marker({
        position: currentPos, 
        image: markerImage // 마커이미지 설정 
    });

    
    // 기존에 마커가 있다면 제거
    marker.setMap(null);
    marker.setMap(map);
    
};

function locationLoadError(pos){
    alert('위치 정보를 가져오는데 실패했습니다.');
};


////////////////////////////////////////////////////////////////////////////////////

// 맵에 마커 띄우기

var markerImageSrc = 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/category.png';
        potholeMarkers = [], // 포트홀 마커 객체를 가지고 있을 배열
        bumpMarkers = [],
        parkingMarkers = [],

        
    createPotholeMarkers(); // 포트홀 마커를 생성하고 포트홀 마커 배열에 추가
    createBumpMarkers();
    createParkingMarkers();



// 마커이미지의 주소와, 크기, 옵션으로 마커 이미지를 생성하여 리턴
function createMarkerImage(src, size, options) {
    var markerImage = new kakao.maps.MarkerImage(src, size, options);
    return markerImage;            
}

// 좌표와 마커이미지를 받아 마커를 생성하여 리턴
function createMarker(position, image) {
    var marker = new kakao.maps.Marker({
        position: position,
        image: image
    });
    
    return marker;  
}   

// 포트홀 마커를 생성하고 포트홀 마커 배열에 추가
function createPotholeMarkers() {
    
    for (var i = 0; i < potholePositions.length; i++) {  
        
        var imageSize = new kakao.maps.Size(26, 26); 
        
        var markerSrc = '../../assets/pothole_color.png'

        // 마커이미지와 마커 생성
        var markerImage = createMarkerImage(markerSrc, imageSize),    
            marker = createMarker(potholePositions[i], markerImage);  
        
        // 생성된 마커를 포트홀 마커 배열에 추가
        potholeMarkers.push(marker);
    }     
}

// 포트홀 마커들의 지도 표시 여부를 설정
function setPotholeMarkers(map) {        
    for (var i = 0; i < potholeMarkers.length; i++) {  
        potholeMarkers[i].setMap(map);
    }        
}

function createBumpMarkers() {
    for (var i = 0; i < bumpPositions.length; i++) {
        
        var imageSize = new kakao.maps.Size(26, 26);       

        var markerSrc = '../../assets/bump_color.png'

        var markerImage = createMarkerImage(markerSrc, imageSize),    
            marker = createMarker(bumpPositions[i], markerImage);  

        bumpMarkers.push(marker);    
    }        
}

function setBumpMarkers(map) {        
    for (var i = 0; i < bumpMarkers.length; i++) {  
        bumpMarkers[i].setMap(map);
    }        
}

// 주차장 마커를 생성하고 주차장 마커 배열에 추가
function createParkingMarkers() {
    
    for (var i = 0; i < parkingPositions.length; i++) {  
        
        var imageSize = new kakao.maps.Size(28, 28); 
        
        var markerSrc = '../../assets/parking_color.png'

        // 마커이미지와 마커 생성
        var markerImage = createMarkerImage(markerSrc, imageSize),    
            marker = createMarker(parkingPositions[i], markerImage);  
        
        // 생성된 마커를 주차장 마커 배열에 추가
        parkingMarkers.push(marker);
    }     
}

// 주차장 마커들의 지도 표시 여부를 설정
function setParkingMarkers(map) {        
    for (var i = 0; i < parkingMarkers.length; i++) {  
        parkingMarkers[i].setMap(map);
    }        
}

///////////////////////////////////////////////////////////////////////////////////
// 어린이 보호 구역 : gps 제대로 설정
function createSchoolZone(){
    if (circle) {
        circle.setMap(null);
    }

    circle = new kakao.maps.Circle({
        center : new kakao.maps.LatLng(37.552727, 127.072662),  // 원의 중심좌표
        radius: 300, // 미터 단위의 원의 반지름
        strokeWeight: 1, // 선 두께
        strokeColor: '#75B8FA', // 선 색깔
        strokeOpacity: 1, // 선의 불투명도 (1에서 0 사이의 값이며 0에 가까울수록 투명)
        strokeStyle: 'dashed', // 선의 스타일
        fillColor: '#CFE7FF', // 채우기 색깔
        fillOpacity: 0.5  // 채우기 불투명도   
    }); 
    circle.setMap(map);
}



function changeMarker(type){

    //console.log("!!!!!!")
        
    var allMenu = document.getElementById('allMenu');
    var potholeMenu = document.getElementById('potholeMenu');
    var bumpMenu = document.getElementById('bumpMenu');
    var schoolMenu = document.getElementById('schoolMenu');


    if (type === 'all'){
        allMenu.className = 'menu_selected';
        potholeMenu.className = '';
        bumpMenu.className = '';
        schoolMenu.className = '';
        
        // 모두 표시
        setPotholeMarkers(map);
        setBumpMarkers(map);
        setParkingMarkers(map);

        createSchoolZone();
    }
    else if (type === 'pothole') {
    
        allMenu.className = '';
        potholeMenu.className = 'menu_selected';
        bumpMenu.className = '';
        schoolMenu.className = '';
        
        setPotholeMarkers(map);
        setBumpMarkers(null);
        setParkingMarkers(map);

        circle.setMap(null);
    } 
    else if (type === 'bump') {

        allMenu.className = '';
        potholeMenu.className = '';
        bumpMenu.className = 'menu_selected';
        schoolMenu.className = '';
        
        setPotholeMarkers(null);
        setBumpMarkers(map);
        setParkingMarkers(map);

        circle.setMap(null);
        
    } 
    else if (type === 'school'){
        allMenu.className = '';
        potholeMenu.className = '';
        bumpMenu.className = '';
        schoolMenu.className = 'menu_selected';

        setPotholeMarkers(null);
        setBumpMarkers(null);
        setParkingMarkers(map);

        createSchoolZone();
    }
}