hljs.registerLanguage('httprequest', function (hljs) {
  return {
    name: 'HTTPRequest',
    contains: [
      {
        className: 'http-method method-get',
        begin: /\bGET\b/
      },
      {
        className: 'http-method method-post',
        begin: /\bPOST\b/
      },
      {
        className: 'http-method method-put',
        begin: /\bPUT\b/
      },
      {
        className: 'http-method method-delete',
        begin: /\bDELETE\b/
      },
      {
        className: 'http-url',
        begin: /\bhttps?:\/\/[^\s]+/
      },
      {
        begin: /\{/,
        end: /\}\s*$/,
        subLanguage: 'json',
        relevance: 0
      }
    ]
  };
});

hljs.registerLanguage('httpresponse', function (hljs) {
  return {
    name: 'HTTPResponse',
    contains: [
      {
        className: 'http-status status-info',
        begin: /^1\d{2}/
      },
      {
        className: 'http-status status-success',
        begin: /^2\d{2}/
      },
      {
        className: 'http-status status-redirect',
        begin: /^3\d{2}/
      },
      {
        className: 'http-status status-client',
        begin: /^4\d{2}/
      },
      {
        className: 'http-status status-server',
        begin: /^5\d{2}/
      },
      {
        className: 'status-text',
        begin: /(?<=^\d{3}\s).*(?=\s*{)/,
        end: /(?=\s*{)/
      },
      {
        className: 'string',
        begin: /(?<=^\d{3}\s).*(?=\s*{)/,
        end: /(?=\s*{)/
      },
      {
        begin: /\{/,
        end: /\}/,
        subLanguage: 'json',
        relevance: 0
      }
    ]
  };
});