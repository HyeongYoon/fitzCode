document.addEventListener("DOMContentLoaded", () => {
    // 계좌 추가 모달 여닫기
    const addAccountModal = document.getElementById("add-account-modalContainer");
    const insertBtn = document.getElementById("insert-btn");

    insertBtn.addEventListener("click", (event) => {
        event.preventDefault();

        // 모달 창 열기
        addAccountModal.classList.remove("hidden");
        document.body.style.overflow = "hidden";

        // 모달 초기화 (값 초기화)
        document.querySelectorAll(".accountHolder").forEach(input => input.value = "");
        document.querySelectorAll(".accountNumber").forEach(input => input.value = "");
        document.querySelectorAll(".checkBtn").forEach(btn => {
            btn.style.background = "white";
            btn.style.color = "black";
            btn.style.border = "#ddd 1px solid";
        });
    });

    // 모달창 닫기
    function closeModal() {
        addAccountModal.classList.add("hidden");
        document.body.style.overflow = "auto";
    }

    document.getElementById("add-account-modalCloseButton").addEventListener("click", closeModal);
    document.getElementById("saveBtn").addEventListener("click", closeModal);

    // 계좌 추가
    document.getElementById("saveBtn").addEventListener("click", () => {
        let accountHolder = document.getElementById("addAccountHolder").value;
        let bankName = document.getElementById("addBankName").value;
        let accountNumber = document.getElementById("addAccountNumber").value;
        let userId = document.getElementById("userId").value;
        console.log(accountHolder);
        console.log(bankName);
        console.log(accountNumber);
        console.log(userId);
        let data = {
            accountHolder: accountHolder,
            bankName: bankName,
            accountNumber: accountNumber,
            userId: userId,
            isDefault: false
        };

        fetch(`${window.location.origin}/mypage/insertAccount`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        })
            .then(data => {
                location.href = `${window.location.origin}/mypage/account`;
            })
            .catch(error => {
                console.error("계좌 추가 실패:", error);
            });
    });

    // 클립보드 계좌 복사
    var defaultAccountClipboard = new ClipboardJS('.copyDefaultAccountNumberBtn');

    defaultAccountClipboard.on('success', function(e) {
        console.info('Action:', e.action);
        console.info('Text:', e.text);
        console.info('Trigger:', e.trigger);
        alert("계좌번호가 복사되었습니다!");

        e.clearSelection();
    });

    defaultAccountClipboard.on('error', function(e) {
        console.error('Action:', e.action);
        console.error('Trigger:', e.trigger);
    });

    var accountClipboard = new ClipboardJS('.copyAccountNumberBtn');

    accountClipboard.on('success', function(e) {
        console.info('Action:', e.action);
        console.info('Text:', e.text);
        console.info('Trigger:', e.trigger);
        alert("계좌번호가 복사되었습니다!");

        e.clearSelection();
    });

    accountClipboard.on('error', function(e) {
        console.error('Action:', e.action);
        console.error('Trigger:', e.trigger);
    });
});