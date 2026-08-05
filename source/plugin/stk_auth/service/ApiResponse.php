<?php
if (!defined('IN_DISCUZ')) { exit('Access Denied'); }

final class StkApiException extends RuntimeException {
    private int $httpStatus;
    private string $publicMessage;

    public function __construct(int $httpStatus, string $code, string $publicMessage) {
        parent::__construct($code);
        $this->httpStatus = $httpStatus;
        $this->publicMessage = $publicMessage;
    }

    public function httpStatus(): int { return $this->httpStatus; }
    public function publicMessage(): string { return $this->publicMessage; }
}

final class StkApiResponse {
    private static ?string $requestIdValue = null;

    public static function send(int $httpStatus, string $code, string $message, array $data = []): void {
        http_response_code($httpStatus);
        header('Content-Type: application/json; charset=utf-8');
        header('Cache-Control: no-store');
        echo json_encode([
            'code' => $code,
            'message' => $message,
            'data' => $data,
            'request_id' => self::requestId(),
            'server_time' => gmdate('c'),
        ], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        exit;
    }

    public static function requestId(): string {
        if (self::$requestIdValue !== null) return self::$requestIdValue;
        $value = $_SERVER['HTTP_X_REQUEST_ID'] ?? '';
        self::$requestIdValue = preg_match('/^[A-Za-z0-9_-]{8,80}$/', $value) ? $value : bin2hex(random_bytes(16));
        return self::$requestIdValue;
    }

    public static function fail(StkApiException $error): void {
        self::send($error->httpStatus(), $error->getMessage(), $error->publicMessage());
    }
}
