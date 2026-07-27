declare namespace API {
  type AsyncContext = {
    response?: ServletResponse
    request?: ServletRequest
    timeout?: number
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type Cookie = {
    name?: string
    value?: string
    attributes?: Record<string, any>
    path?: string
    comment?: string
    version?: number
    domain?: string
    maxAge?: number
    secure?: boolean
    httpOnly?: boolean
  }

  type DeleteRequest = {
    id?: number
  }

  type FilterRegistration = {
    servletNameMappings?: string[]
    urlPatternMappings?: string[]
    name?: string
    className?: string
    initParameters?: Record<string, any>
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type HttpServletMapping = {
    matchValue?: string
    pattern?: string
    servletName?: string
    mappingMatch?: 'CONTEXT_ROOT' | 'DEFAULT' | 'EXACT' | 'EXTENSION' | 'PATH'
  }

  type HttpServletRequest = {
    method?: string
    contextPath?: string
    session?: HttpSession
    remoteUser?: string
    userPrincipal?: { name?: string }
    headerNames?: Record<string, any>
    queryString?: string
    requestURI?: string
    parts?: Part[]
    authType?: string
    cookies?: Cookie[]
    requestedSessionIdFromCookie?: boolean
    requestedSessionIdFromURL?: boolean
    httpServletMapping?: HttpServletMapping
    requestedSessionId?: string
    requestedSessionIdValid?: boolean
    trailerFieldsReady?: boolean
    trailerFields?: Record<string, any>
    pathTranslated?: string
    servletPath?: string
    pathInfo?: string
    requestURL?: { length?: number; empty?: boolean }
    scheme?: string
    inputStream?: ServletInputStream
    protocol?: string
    locale?: {
      language?: string
      displayName?: string
      country?: string
      variant?: string
      script?: string
      unicodeLocaleAttributes?: string[]
      unicodeLocaleKeys?: string[]
      displayLanguage?: string
      displayScript?: string
      displayCountry?: string
      displayVariant?: string
      extensionKeys?: string[]
      iso3Language?: string
      iso3Country?: string
    }
    contentLength?: number
    contentLengthLong?: number
    contentType?: string
    localName?: string
    localPort?: number
    attributeNames?: Record<string, any>
    servletContext?: ServletContext
    reader?: Record<string, any>
    parameterNames?: Record<string, any>
    characterEncoding?: string
    servletConnection?: ServletConnection
    protocolRequestId?: string
    asyncStarted?: boolean
    asyncContext?: AsyncContext
    requestId?: string
    serverName?: string
    serverPort?: number
    remotePort?: number
    dispatcherType?: 'FORWARD' | 'INCLUDE' | 'REQUEST' | 'ASYNC' | 'ERROR'
    parameterMap?: Record<string, any>
    remoteHost?: string
    localAddr?: string
    asyncSupported?: boolean
    remoteAddr?: string
    secure?: boolean
    locales?: Record<string, any>
  }

  type HttpSession = {
    id?: string
    creationTime?: number
    attributeNames?: Record<string, any>
    servletContext?: ServletContext
    maxInactiveInterval?: number
    lastAccessedTime?: number
    new?: boolean
  }

  type JspConfigDescriptor = {
    jspPropertyGroups?: JspPropertyGroupDescriptor[]
    taglibs?: TaglibDescriptor[]
  }

  type JspPropertyGroupDescriptor = {
    buffer?: string
    defaultContentType?: string
    urlPatterns?: string[]
    errorOnELNotFound?: string
    scriptingInvalid?: string
    pageEncoding?: string
    includePreludes?: string[]
    includeCodas?: string[]
    elIgnored?: string
    isXml?: string
    deferredSyntaxAllowedAsLiteral?: string
    trimDirectiveWhitespaces?: string
    errorOnUndeclaredNamespace?: string
  }

  type LoginUserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type Part = {
    name?: string
    size?: number
    inputStream?: Record<string, any>
    submittedFileName?: string
    contentType?: string
    headerNames?: string[]
  }

  type ReadListener = true

  type ServletConnection = {
    protocol?: string
    connectionId?: string
    secure?: boolean
    protocolConnectionId?: string
  }

  type ServletContext = {
    classLoader?: {
      name?: string
      registeredAsParallelCapable?: boolean
      parent?: {
        name?: string
        registeredAsParallelCapable?: boolean
        unnamedModule?: {
          name?: string
          descriptor?: { open?: boolean; automatic?: boolean }
          named?: boolean
          annotations?: Record<string, any>[]
          declaredAnnotations?: Record<string, any>[]
          packages?: string[]
          nativeAccessEnabled?: boolean
          layer?: Record<string, any>
        }
        definedPackages?: {
          name?: string
          annotations?: Record<string, any>[]
          declaredAnnotations?: Record<string, any>[]
          sealed?: boolean
          specificationTitle?: string
          specificationVersion?: string
          specificationVendor?: string
          implementationTitle?: string
          implementationVersion?: string
          implementationVendor?: string
        }[]
        defaultAssertionStatus?: boolean
      }
      unnamedModule?: {
        name?: string
        descriptor?: { open?: boolean; automatic?: boolean }
        named?: boolean
        annotations?: Record<string, any>[]
        declaredAnnotations?: Record<string, any>[]
        packages?: string[]
        nativeAccessEnabled?: boolean
        layer?: Record<string, any>
      }
      definedPackages?: {
        name?: string
        annotations?: Record<string, any>[]
        declaredAnnotations?: Record<string, any>[]
        sealed?: boolean
        specificationTitle?: string
        specificationVersion?: string
        specificationVendor?: string
        implementationTitle?: string
        implementationVersion?: string
        implementationVendor?: string
      }[]
      defaultAssertionStatus?: boolean
    }
    majorVersion?: number
    minorVersion?: number
    attributeNames?: Record<string, any>
    contextPath?: string
    initParameterNames?: Record<string, any>
    virtualServerName?: string
    sessionCookieConfig?: SessionCookieConfig
    sessionTimeout?: number
    serverInfo?: string
    defaultSessionTrackingModes?: ('COOKIE' | 'URL' | 'SSL')[]
    effectiveSessionTrackingModes?: ('COOKIE' | 'URL' | 'SSL')[]
    requestCharacterEncoding?: string
    responseCharacterEncoding?: string
    effectiveMajorVersion?: number
    effectiveMinorVersion?: number
    servletContextName?: string
    jspConfigDescriptor?: JspConfigDescriptor
    servletRegistrations?: Record<string, any>
    filterRegistrations?: Record<string, any>
    sessionTrackingModes?: ('COOKIE' | 'URL' | 'SSL')[]
  }

  type ServletInputStream = {
    finished?: boolean
    ready?: boolean
    readListener?: ReadListener
  }

  type ServletOutputStream = {
    ready?: boolean
    writeListener?: WriteListener
  }

  type ServletRegistration = {
    mappings?: string[]
    runAsRole?: string
    name?: string
    className?: string
    initParameters?: Record<string, any>
  }

  type ServletRequest = {
    scheme?: string
    inputStream?: ServletInputStream
    protocol?: string
    locale?: {
      language?: string
      displayName?: string
      country?: string
      variant?: string
      script?: string
      unicodeLocaleAttributes?: string[]
      unicodeLocaleKeys?: string[]
      displayLanguage?: string
      displayScript?: string
      displayCountry?: string
      displayVariant?: string
      extensionKeys?: string[]
      iso3Language?: string
      iso3Country?: string
    }
    contentLength?: number
    contentLengthLong?: number
    contentType?: string
    localName?: string
    localPort?: number
    attributeNames?: Record<string, any>
    servletContext?: ServletContext
    reader?: Record<string, any>
    parameterNames?: Record<string, any>
    characterEncoding?: string
    servletConnection?: ServletConnection
    protocolRequestId?: string
    asyncStarted?: boolean
    asyncContext?: AsyncContext
    requestId?: string
    serverName?: string
    serverPort?: number
    remotePort?: number
    dispatcherType?: 'FORWARD' | 'INCLUDE' | 'REQUEST' | 'ASYNC' | 'ERROR'
    parameterMap?: Record<string, any>
    remoteHost?: string
    localAddr?: string
    asyncSupported?: boolean
    remoteAddr?: string
    secure?: boolean
    locales?: Record<string, any>
  }

  type ServletResponse = {
    locale?: {
      language?: string
      displayName?: string
      country?: string
      variant?: string
      script?: string
      unicodeLocaleAttributes?: string[]
      unicodeLocaleKeys?: string[]
      displayLanguage?: string
      displayScript?: string
      displayCountry?: string
      displayVariant?: string
      extensionKeys?: string[]
      iso3Language?: string
      iso3Country?: string
    }
    contentType?: string
    contentLength?: number
    outputStream?: ServletOutputStream
    bufferSize?: number
    characterEncoding?: string
    contentLengthLong?: number
    committed?: boolean
    writer?: Record<string, any>
  }

  type SessionCookieConfig = {
    name?: string
    path?: string
    attributes?: Record<string, any>
    comment?: string
    domain?: string
    maxAge?: number
    secure?: boolean
    httpOnly?: boolean
  }

  type TaglibDescriptor = {
    taglibLocation?: string
    taglibURI?: string
  }

  type User = {
    id?: number
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserAddRequest = {
    userName?: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserUpdateRequest = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }

  type WriteListener = true
}
