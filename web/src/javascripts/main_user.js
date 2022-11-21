function modal(id) {
	var zIndex = 20519;
	var modal = document.getElementById(id);

	// 모달 div 뒤에 희끄무레한 레이어
	var bg = document.createElement('div');
	bg.setStyle({
		position: 'fixed',
		zIndex: zIndex,
		left: '0px',
		top: '0px',
		width: '100%',
		height: '100%',
		overflow: 'auto',
		// 레이어 색갈은 여기서 바꾸면 됨
		backgroundColor: 'rgba(0,0,0,0.4)'
	});
	document.body.append(bg);

	// 닫기 버튼 처리, 시꺼먼 레이어와 모달 div 지우기
	modal.querySelector('.modal_close_btn').addEventListener('click', function() {
		bg.remove();
		modal.style.display = 'none';
	});

	modal.setStyle({
		position: 'fixed',
		display: 'block',
		boxShadow: '0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.120519)',

		// 시꺼먼 레이어 보다 한칸 위에 보이기
		zIndex: zIndex + 1,

		// div center 정렬
		top: '50%',
		left: '50%',
		transform: 'translate(-50%, -50%)',
		msTransform: 'translate(-50%, -50%)',
		webkitTransform: 'translate(-50%, -50%)'
	});
}

// Element 에 style 한번에 오브젝트로 설정하는 함수 추가
Element.prototype.setStyle = function(styles) {
	for (var k in styles) this.style[k] = styles[k];
	return this;
};

var num = 0


document.getElementById('user_1').onclick = function() { modal('my_modal'); num = 1}
document.getElementById('user_2').onclick = function() { modal('my_modal'); num = 2}
document.getElementById('user_3').onclick = function() { modal('my_modal'); num = 3}
document.getElementById('user_4').onclick = function() { modal('my_modal'); num = 4}
document.getElementById('user_5').onclick = function() { modal('my_modal'); num = 5}
document.getElementById('user_6').onclick = function() { modal('my_modal'); num = 6}

var myHeaders = new Headers();
myHeaders.append("Accept", "application/json");
myHeaders.append("X-M2M-RI", "12345");
myHeaders.append("X-M2M-Origin", "SOrigin");

var requestOptions = {
	method: 'GET',
	headers: myHeaders,
	redirect: 'follow'
};


setInterval(function(){
	ID_list = []

	fetch("http://203.250.148.120:20519/Mobius/kick_off/user/account?fu=1&ty=4", requestOptions)
		.then(response => response.json())
		.then(result => {

			fetch("http://203.250.148.120:20519/Mobius/kick_off/user/account/la", requestOptions)
				.then(response => response.json())
				.then(result => {
					var rst_list = result["m2m:cin"]["con"].split(" ")
					// console.log(rst_list)
					document.getElementById("u1_1").innerHTML = rst_list[0]
					document.getElementById("u1_2").innerHTML = rst_list[1]
					document.getElementById("u1_3").innerHTML = rst_list[2]
					document.getElementById("u1_4").innerHTML = rst_list[4]
					document.getElementById("u1_5").innerHTML = rst_list[5]
					document.getElementById("u1_6").innerHTML = rst_list[6]
	
					if (num == 1){
						document.getElementById("m_1").innerHTML = rst_list[0]
						document.getElementById("m_2").innerHTML = rst_list[1]
						document.getElementById("m_3").innerHTML = rst_list[2]
						document.getElementById("m_4").innerHTML = rst_list[4]
						document.getElementById("m_5").innerHTML = rst_list[5]
						document.getElementById("m_6").innerHTML = rst_list[6]
		
						document.getElementById("rating").innerHTML = rst_list[6]
						document.getElementById("safety_danger").innerHTML = rst_list[7]
		
						document.getElementById("sudden_stop").innerHTML = rst_list[9]
						document.getElementById("buff_speed").innerHTML = rst_list[10]
						document.getElementById("schoolzone_speed").innerHTML = rst_list[11]
					}
				})
				.catch(error => console.log('error', error));
	
			ID_list = result["m2m:uril"][1].split("/")[4]

			fetch("http://203.250.148.120:20519/Mobius/kick_off/user/account/" + ID_list, requestOptions)
			.then(response => response.json())
			.then(result => {
				var rst_list = result["m2m:cin"]["con"].split(" ")
				// console.log(rst_list)
				document.getElementById("u2_1").innerHTML = rst_list[0]
				document.getElementById("u2_2").innerHTML = rst_list[1]
				document.getElementById("u2_3").innerHTML = rst_list[2]
				document.getElementById("u2_4").innerHTML = rst_list[4]
				document.getElementById("u2_5").innerHTML = rst_list[5]
				document.getElementById("u2_6").innerHTML = rst_list[6]

				if (num == 2){
					document.getElementById("m_1").innerHTML = rst_list[0]
					document.getElementById("m_2").innerHTML = rst_list[1]
					document.getElementById("m_3").innerHTML = rst_list[2]
					document.getElementById("m_4").innerHTML = rst_list[4]
					document.getElementById("m_5").innerHTML = rst_list[5]
					document.getElementById("m_6").innerHTML = rst_list[6]
	
					document.getElementById("rating").innerHTML = rst_list[6]
					document.getElementById("safety_danger").innerHTML = rst_list[7]
	
					document.getElementById("sudden_stop").innerHTML = rst_list[9]
					document.getElementById("buff_speed").innerHTML = rst_list[10]
					document.getElementById("schoolzone_speed").innerHTML = rst_list[11]
				}

			})

			ID_list = result["m2m:uril"][2].split("/")[4]

			fetch("http://203.250.148.120:20519/Mobius/kick_off/user/account/" + ID_list, requestOptions)
			.then(response => response.json())
			.then(result => {
				var rst_list = result["m2m:cin"]["con"].split(" ")
				// console.log(rst_list)
				document.getElementById("u3_1").innerHTML = rst_list[0]
				document.getElementById("u3_2").innerHTML = rst_list[1]
				document.getElementById("u3_3").innerHTML = rst_list[2]
				document.getElementById("u3_4").innerHTML = rst_list[4]
				document.getElementById("u3_5").innerHTML = rst_list[5]
				document.getElementById("u3_6").innerHTML = rst_list[6]

				if (num == 3){
					document.getElementById("m_1").innerHTML = rst_list[0]
					document.getElementById("m_2").innerHTML = rst_list[1]
					document.getElementById("m_3").innerHTML = rst_list[2]
					document.getElementById("m_4").innerHTML = rst_list[4]
					document.getElementById("m_5").innerHTML = rst_list[5]
					document.getElementById("m_6").innerHTML = rst_list[6]
	
					document.getElementById("rating").innerHTML = rst_list[6]
					document.getElementById("safety_danger").innerHTML = rst_list[7]
	
					document.getElementById("sudden_stop").innerHTML = rst_list[9]
					document.getElementById("buff_speed").innerHTML = rst_list[10]
					document.getElementById("schoolzone_speed").innerHTML = rst_list[11]
				}

			})

			ID_list = result["m2m:uril"][3].split("/")[4]

			fetch("http://203.250.148.120:20519/Mobius/kick_off/user/account/" + ID_list, requestOptions)
			.then(response => response.json())
			.then(result => {
				var rst_list = result["m2m:cin"]["con"].split(" ")
				// console.log(rst_list)
				document.getElementById("u4_1").innerHTML = rst_list[0]
				document.getElementById("u4_2").innerHTML = rst_list[1]
				document.getElementById("u4_3").innerHTML = rst_list[2]
				document.getElementById("u4_4").innerHTML = rst_list[4]
				document.getElementById("u4_5").innerHTML = rst_list[5]
				document.getElementById("u4_6").innerHTML = rst_list[6]

				if (num == 4){
					document.getElementById("m_1").innerHTML = rst_list[0]
					document.getElementById("m_2").innerHTML = rst_list[1]
					document.getElementById("m_3").innerHTML = rst_list[2]
					document.getElementById("m_4").innerHTML = rst_list[4]
					document.getElementById("m_5").innerHTML = rst_list[5]
					document.getElementById("m_6").innerHTML = rst_list[6]
	
					document.getElementById("rating").innerHTML = rst_list[6]
					document.getElementById("safety_danger").innerHTML = rst_list[7]
	
					document.getElementById("sudden_stop").innerHTML = rst_list[9]
					document.getElementById("buff_speed").innerHTML = rst_list[10]
					document.getElementById("schoolzone_speed").innerHTML = rst_list[11]
				}

			})

			ID_list = result["m2m:uril"][4].split("/")[4]

			fetch("http://203.250.148.120:20519/Mobius/kick_off/user/account/" + ID_list, requestOptions)
			.then(response => response.json())
			.then(result => {
				var rst_list = result["m2m:cin"]["con"].split(" ")
				// console.log(rst_list)
				document.getElementById("u5_1").innerHTML = rst_list[0]
				document.getElementById("u5_2").innerHTML = rst_list[1]
				document.getElementById("u5_3").innerHTML = rst_list[2]
				document.getElementById("u5_4").innerHTML = rst_list[4]
				document.getElementById("u5_5").innerHTML = rst_list[5]
				document.getElementById("u5_6").innerHTML = rst_list[6]

				if (num == 5){
					document.getElementById("m_1").innerHTML = rst_list[0]
					document.getElementById("m_2").innerHTML = rst_list[1]
					document.getElementById("m_3").innerHTML = rst_list[2]
					document.getElementById("m_4").innerHTML = rst_list[4]
					document.getElementById("m_5").innerHTML = rst_list[5]
					document.getElementById("m_6").innerHTML = rst_list[6]
	
					document.getElementById("rating").innerHTML = rst_list[6]
					document.getElementById("safety_danger").innerHTML = rst_list[7]
	
					document.getElementById("sudden_stop").innerHTML = rst_list[9]
					document.getElementById("buff_speed").innerHTML = rst_list[10]
					document.getElementById("schoolzone_speed").innerHTML = rst_list[11]
				}

			})

			ID_list = result["m2m:uril"][5].split("/")[4]

			fetch("http://203.250.148.120:20519/Mobius/kick_off/user/account/" + ID_list, requestOptions)
			.then(response => response.json())
			.then(result => {
				var rst_list = result["m2m:cin"]["con"].split(" ")
				// console.log(rst_list)
				document.getElementById("u6_1").innerHTML = rst_list[0]
				document.getElementById("u6_2").innerHTML = rst_list[1]
				document.getElementById("u6_3").innerHTML = rst_list[2]
				document.getElementById("u6_4").innerHTML = rst_list[4]
				document.getElementById("u6_5").innerHTML = rst_list[5]
				document.getElementById("u6_6").innerHTML = rst_list[6]

				if (num == 6){
					document.getElementById("m_1").innerHTML = rst_list[0]
					document.getElementById("m_2").innerHTML = rst_list[1]
					document.getElementById("m_3").innerHTML = rst_list[2]
					document.getElementById("m_4").innerHTML = rst_list[4]
					document.getElementById("m_5").innerHTML = rst_list[5]
					document.getElementById("m_6").innerHTML = rst_list[6]
	
					document.getElementById("rating").innerHTML = rst_list[6]
					document.getElementById("safety_danger").innerHTML = rst_list[7]
	
					document.getElementById("sudden_stop").innerHTML = rst_list[9]
					document.getElementById("buff_speed").innerHTML = rst_list[10]
					document.getElementById("schoolzone_speed").innerHTML = rst_list[11]
				}

			})

			
		})
		.catch(error => console.log('error', error));
		
	
}, 100)
