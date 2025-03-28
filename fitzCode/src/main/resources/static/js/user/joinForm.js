// 유효성 검사
function validateForm() {
    var password = document.getElementById('password').value;
    var passwordConfirm = document.getElementById('password_confirm').value;
    var passwordError = document.getElementById('passwordError');
    var passwordLengthError = document.getElementById('passwordLengthError');
    var phoneNumber = document.getElementById('phoneNumber').value;
    var phoneNumberError = document.getElementById('phoneNumberError');
    var birthDate = document.getElementsByName('birthDate')[0].value;
    var birthDateError = document.getElementById('birthDateError');

    // 비밀번호 길이 검사 (8~16자리)
    if (password.length < 8 || password.length > 16) {
        passwordLengthError.textContent = '비밀번호는 최소 8자리 최대 16자리 입니다.';
        passwordLengthError.style.color = 'red';
        return false;
    } else {
        passwordLengthError.textContent = '';
    }

    // 비밀번호와 비밀번호 확인 일치 체크
    if (password !== passwordConfirm) {
        passwordError.textContent = '비밀번호가 일치하지 않습니다.';
        passwordError.style.color = 'red';
        return false;
    } else {
        passwordError.textContent = '비밀번호가 일치합니다.';
        passwordError.style.color = 'green';
    }

    // 전화번호 형식
    const phoneRegex = /^010-\d{4}-\d{4}$/;
    if (!phoneRegex.test(phoneNumber)) {
        phoneNumberError.textContent = '전화번호는 010-XXXX-XXXX 형식이어야 합니다.';
        phoneNumberError.style.color = 'red';
        return false;
    } else {
        phoneNumberError.textContent = '';
    }

    // 생년월일 형식
    const birthDateRegex = /^\d{8}$/;
    if (!birthDateRegex.test(birthDate)) {
        birthDateError.textContent = '생년월일은 8자리입니다.';
        birthDateError.style.color = 'red';
        return false;
    } else {
        birthDateError.textContent = '';
    }

    return true;
}

// 페이지 로드 시 이벤트
document.addEventListener("DOMContentLoaded", function () {
    const phoneNumberInput = document.getElementById('phoneNumber');
    const passwordInput = document.getElementById('password');
    const passwordConfirmInput = document.getElementById('password_confirm');
    const birthDateInput = document.getElementsByName('birthDate')[0];
    const passwordLengthError = document.getElementById('passwordLengthError');
    const passwordError = document.getElementById('passwordError');
    const birthDateError = document.getElementById('birthDateError');
    let timeoutIdPassword;
    let timeoutIdBirthDate;

    // 비밀번호 입력 시 실시간 유효성 검사
    passwordInput.addEventListener('input', function (e) {
        let password = e.target.value;
        if (password.length < 8 || password.length > 16) {
            passwordLengthError.textContent = '비밀번호는 최소 8자리 최대 16자리 입니다.';
            passwordLengthError.style.color = 'red';
        } else {
            passwordLengthError.textContent = '';
        }
    });

    // 비밀번호 확인 입력 시 1초 후 일치 여부 검사 (일치 시 바로 표시)
    passwordConfirmInput.addEventListener('input', function (e) {
        clearTimeout(timeoutIdPassword);

        let password = document.getElementById('password').value;
        let passwordConfirm = e.target.value;

        // 비밀번호가 일치하면 바로 메시지 표시
        if (password === passwordConfirm && passwordConfirm !== '') {
            passwordError.textContent = '비밀번호가 일치합니다.';
            passwordError.style.color = 'green';
        } else {
            // 일치하지 않으면 1초 후 메시지 표시
            timeoutIdPassword = setTimeout(function () {
                if (password !== passwordConfirm) {
                    passwordError.textContent = '비밀번호가 일치하지 않습니다.';
                    passwordError.style.color = 'red';
                }
            }, 1000);
        }
    });

    // 생년월일 입력 시 1초 후 유효성 검사
    birthDateInput.addEventListener('input', function (e) {
        clearTimeout(timeoutIdBirthDate);

        let birthDate = e.target.value;

        // 1초 후 유효성 검사
        timeoutIdBirthDate = setTimeout(function () {
            const birthDateRegex = /^\d{8}$/;
            if (!birthDateRegex.test(birthDate)) {
                birthDateError.textContent = '생년월일은 8자리입니다.';
                birthDateError.style.color = 'red';
            } else {
                birthDateError.textContent = '';
            }
        }, 1000);
    });

    // 전화번호 입력 처리
    phoneNumberInput.addEventListener('input', function (e) {
        let value = e.target.value.replace(/[^0-9]/g, ''); // 숫자만 허용
        if (value.length > 3) {
            value = value.substring(0, 3) + '-' + value.substring(3); // 010-
        }
        if (value.length > 8) {
            value = value.substring(0, 8) + '-' + value.substring(8, 12); // 010-XXXX-
        }
        if (value.length > 13) {
            value = value.substring(0, 13); // 최대 길이 13자리 (010-XXXX-XXXX)
        }
        e.target.value = value;
    });

    // 입력 시작 시 010- 고정
    phoneNumberInput.addEventListener('focus', function () {
        if (!phoneNumberInput.value.startsWith('010-')) {
            phoneNumberInput.value = '010-';
        }
    });

    // 전체 동의 체크 시 모든 항목 체크
    const agreeAll = document.getElementById("agree-all");
    const agreeChecks = document.querySelectorAll(".agree-check[data-required='true']");
    const signupBtn = document.getElementById("signup-btn");

    agreeAll.addEventListener("change", function () {
        document.querySelectorAll(".agree-check").forEach(checkbox => {
            checkbox.checked = agreeAll.checked;
        });
        toggleSignupButton();
    });

    // 개별 체크박스 변경 시 전체 동의 체크박스 상태 변경
    document.querySelectorAll(".agree-check").forEach(checkbox => {
        checkbox.addEventListener("change", function () {
            agreeAll.checked = [...document.querySelectorAll(".agree-check")].every(cb => cb.checked);
            toggleSignupButton();
        });
    });

    // 필수 항목 체크 여부에 따라 회원가입 버튼 활성화
    function toggleSignupButton() {
        const allChecked = [...agreeChecks].every(cb => cb.checked);
        if (allChecked) {
            signupBtn.classList.add("active");
            signupBtn.removeAttribute("disabled");
        } else {
            signupBtn.classList.remove("active");
            signupBtn.setAttribute("disabled", "true");
        }
    }
});

// 이메일 유효성 검사 함수
function isValidEmail(email) {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email);
}

// 이메일 중복 체크
function checkEmail() {
    let email = document.getElementById("email").value;
    let messageElement = document.getElementById("emailMessage");

    // 이메일이 비어있는지 확인
    if (email.trim() === "") {
        messageElement.textContent = "이메일을 입력하세요";
        messageElement.style.color = "red";
        return;
    }

    // 이메일 형식 유효성 검사
    if (!isValidEmail(email)) {
        messageElement.textContent = "유효한 이메일 형식이 아닙니다.";
        messageElement.style.color = "red";
        return;
    }

    fetch(`/checkEmail?email=${encodeURIComponent(email)}`, {
        method: 'GET'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('서버 응답 오류: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            if (data.available) {
                messageElement.textContent = "사용 가능한 이메일입니다.";
                messageElement.style.color = "green";
            } else {
                messageElement.textContent = "이미 사용 중인 이메일입니다.";
                messageElement.style.color = "red";
            }
        })
        .catch(error => {
            console.error("이메일 중복 확인 오류:", error);
            messageElement.textContent = "이메일 중복 확인 중 오류가 발생했습니다.";
            messageElement.style.color = "red";
        });
}

// 닉네임 중복 체크
function checkNickname() {
    let nickname = document.getElementById("nickname").value;
    let messageElement = document.getElementById("nicknameMessage");

    // 닉네임 비어 있는지 확인
    if (nickname.trim() === "") {
        messageElement.textContent = "닉네임을 입력하세요";
        messageElement.style.color = "red";
        return;
    }

    // 닉네임 길이 3글자 이상인지 확인
    if (nickname.length < 3) {
        messageElement.textContent = "닉네임은 3글자 이상이어야 합니다.";
        messageElement.style.color = "red";
        return;
    }

    fetch(`/checkNickname?nickname=${encodeURIComponent(nickname)}`, {
        method: 'GET'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('서버 응답 오류: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            if (data.available) {
                messageElement.textContent = "사용 가능한 닉네임입니다.";
                messageElement.style.color = "green";
            } else {
                messageElement.textContent = "이미 사용 중인 닉네임입니다.";
                messageElement.style.color = "red";
            }
        })
        .catch(error => {
            console.error("닉네임 중복 확인 오류:", error);
            messageElement.textContent = "닉네임 중복 확인 중 오류가 발생했습니다.";
            messageElement.style.color = "red";
        });
}