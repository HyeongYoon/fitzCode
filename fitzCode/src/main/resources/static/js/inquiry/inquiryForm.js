$(() => {
    const searchProductModal = $("#searchProductModalContainer");
    // 상품 검색 모달 열기
    $("#searchProduct").on("click", () => {
        console.log("버튼 눌림");
        searchProductModal.removeClass("hidden");
        document.body.style.overflow = 'hidden';
    });

    $(".searchProductModalCloseBtn").on("click", () => {
        searchProductModal.addClass("hidden");
        document.body.style.overflow = "auto";
    });

    $("#userInputProductName").on("input", () => {
        let userInputProductName = $("#userInputProductName").val();
        $.ajax({
            type: "GET",
            // 동적으로 도메인 사용
            url: `${window.location.origin}/inquiry/searchProduct`,
            data: {
                userInputProductName: userInputProductName
            },
            success: function (data) {
                console.log("AJAX 요청 성공");
                console.log(data);
                let productData = "";
                $.each(data, function (key, value) {
                    productData += ("<tr class='selectProduct' data-product-id='" + value.productId + "'>");
                    productData += ("<td rowspan='4' style='height: 120px; width: 120px;'>");
                    productData += ("<img src='" + value.imageUrl + "' alt='" + value.productName + "'>");
                    productData += ("</td>");
                    productData += ("</tr>");

                    productData += ("<tr class='selectProduct' data-product-id='" + value.productId + "'>");
                    productData += ("<td style='font-weight: bold;'>" + value.brand + "</td>");
                    productData += ("</tr>");

                    productData += ("<tr class='selectProduct' data-product-id='" + value.productId + "'>");
                    productData += ("<td>" + value.productName + "</td>");
                    productData += ("</tr>");

                    productData += ("<tr style='border-bottom: #888888 1px solid' class='selectProduct' data-product-id='" + value.productId + "'>");
                    productData += ("<td style='color: lightgray'>" + value.description + "</td>");
                    productData += ("</tr>");
                });
                $("#productData").html(productData);
            },
            error: function (xhr, status, error) {
                console.log("AJAX 요청 실패!", xhr, status, error);
                console.log("상태 코드:", xhr.status);  // HTTP 상태 코드 (예: 404, 500 등)
                console.log("응답 텍스트:", xhr.responseText);  // 서버에서 반환한 오류 메시지
            }
        });
    });

    // 주문 내역 불러오기
    const searchOrderListModal = $("#orderListModalContainer");
    // 상품 검색 모달 열기
    $("#searchOrderList").on("click", () => {
        console.log("버튼 눌림");
        searchOrderListModal.removeClass("hidden");
        document.body.style.overflow = 'hidden';
        let userId = $("#userId").val();
        console.log(userId);
        $.ajax({
            type: "GET",
            url: `${window.location.origin}/inquiry/searchOrderList`,
            data: {
                userId: userId
            },
            success: function (data) {
                console.log("AJAX 요청 성공");
                console.log(data);
                let orderList = '';
                $.each(data, function (key, value) {
                    orderList += ("<tr data-product-id='" + value.productId + "' class='orderedProduct'>");
                    orderList += ("<td rowspan='4' style='height: 120px; width: 120px;' class='selectOrderList'>");
                    orderList += ("<input type='hidden' class='productId' value='" + value.productId + "'>");
                    orderList += ("<img src='" + value.imageUrl + "' alt='" + value.productName + "'>");
                    orderList += ("</td>");
                    orderList += ("</tr>");

                    orderList += ("<tr data-product-id='" + value.productId + "' class='orderedProduct'>");
                    orderList += ("<td style='font-weight: bold;' class='selectOrderList'>" + value.brand + "</td>");
                    orderList += ("</tr>");

                    orderList += ("<tr data-product-id='" + value.productId + "' class='orderedProduct'>");
                    orderList += ("<td class='selectProduct'>" + value.productName + "</td>");
                    orderList += ("</tr>");

                    orderList += ("<tr style='border-bottom: #888888 1px solid' class='orderedProduct' data-product-id='" + value.productId + "'>");
                    orderList += ("<td style='color: lightgray' class='selectOrderList'>" + value.description + "</td>");
                    orderList += ("</tr>");
                });
                $("#orderList").html(orderList);
            },
            error: function (xhr, status, error) {
                console.log("AJAX 요청 실패!", xhr, status, error);
                console.log("상태 코드:", xhr.status);
                console.log("응답 텍스트:", xhr.responseText);
            }
        });
    });

    $(".orderListModalCloseBtn").on("click", () => {
        searchOrderListModal.addClass("hidden");
        document.body.style.overflow = "auto";
    });

    // 상품 검색 및 주문 내역에서 상품 form에 띄우기
    $(document).on("click", ".selectProduct, .orderedProduct", function () {
        let productId = $(this).data("product-id");
        console.log(productId);
        $.ajax({
            type: "GET",
            url: `${window.location.origin}/inquiry/selectedProduct`,
            data: {
                productId: productId
            },
            success: function (data) {
                let searchedData = '';
                console.log(data);
                searchedData += ("<tr>");
                searchedData += ("<td rowspan='4' style='height: 120px; width: 120px;'>");
                searchedData += ("<img src='" + data.imageUrl + "' alt='" + data.productName + "'>");
                searchedData += ("</td>");
                searchedData += ("</tr>");

                searchedData += ("<tr>");
                searchedData += ("<td style='font-weight: bold;'>" + data.brand + "</td>");
                searchedData += ("</tr>");

                searchedData += ("<tr>");
                searchedData += ("<td>" + data.productName + "</td>");
                searchedData += ("</tr>");

                searchedData += ("<tr>");
                searchedData += ("<td style='color: lightgray'>" + data.description + "</td>");
                searchedData += ("</tr>");

                searchedData += ("<tr>");
                searchedData += ("<td>");
                searchedData += ("<input type='hidden' value='" + data.productId + "' name='productId'>");
                if (data.orderId != null) {
                    searchedData += ("<input type='hidden' value='" + data.orderId + "' name='orderId'>");
                }
                searchedData += ("</td>");
                searchedData += ("</tr>");

                $("#searchedData").html(searchedData);
            },
            error: function (xhr, status, error) {
                console.log("AJAX 요청 실패!", xhr, status, error);
                console.log("상태 코드:", xhr.status);
                console.log("응답 텍스트:", xhr.responseText);
            }
        });

        if ($(this).hasClass("selectProduct")) {
            searchProductModal.addClass("hidden");
            document.body.style.overflow = "auto";
        } else if ($(this).hasClass("orderedProduct")) {
            searchOrderListModal.addClass("hidden");
            document.body.style.overflow = "auto";
        }
    });

    // 사진 추가
    let cnt = 0;
    $("#addImage").on("click", (e) => {
        e.preventDefault();
        let tag = "<input type='file' id='images' name='files'>";
        $(".imageContainer").append(tag);
        cnt++;
        console.log(cnt);
        if (cnt > 3) {
            let alert = "<p style='color: red;'>사진 첨부는 최대 5장까지 할 수 있습니다.</p>";
            $("#addImage").prop("disabled", true);
            $(".imageContainer").append(alert);
        }
    });
});